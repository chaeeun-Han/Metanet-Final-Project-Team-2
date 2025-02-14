package com.classpick.web.zoom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.zoom.dao.IZoomRepository;
import com.classpick.web.zoom.model.ZoomTokenResponse;
import com.classpick.web.zoom.service.ZoomService;

@ExtendWith(MockitoExtension.class)
class ZoomServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ILectureRepository lectureRepository;

    @Mock
    private IMemberRepository memberRepository;

    @Mock
    private IZoomRepository zoomRepository;

    //@InjectMocks
    private ZoomService zoomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        zoomService = new ZoomService(redisTemplate, memberRepository, zoomRepository, lectureRepository, restTemplate);
    }

    @Test
    void requestZoomAccessToken_ShouldReturnToken_WhenSuccessful() {
        String code = "auth_code";
        String memberId = "testUser";
        Long memberUID = 1L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);

        String mockApiResponse = "{\"access_token\":\"accessToken\",\"refresh_token\":\"refreshToken\"}";
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockApiResponse));

        ZoomTokenResponse result = zoomService.requestZoomAccessToken(code, memberId);

        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());

        verify(valueOperations, times(1)).set("zoom_accessToken:" + memberUID, "accessToken", 1, TimeUnit.HOURS);
        verify(valueOperations, times(1)).set("zoom_refreshToken:" + memberUID, "refreshToken", 30, TimeUnit.DAYS);
    }

    @Test
    void refreshZoomToken_ShouldReturnNewToken_WhenSuccessful() {
        Long memberUID = 1L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn("oldRefreshToken");

        String mockApiResponse = "{\"access_token\":\"newAccessToken\",\"refresh_token\":\"newRefreshToken\"}";
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockApiResponse));

        ZoomTokenResponse result = zoomService.refreshZoomToken(memberUID);

        assertNotNull(result);
        assertEquals("newAccessToken", result.getAccessToken());

        verify(valueOperations, times(1)).set("zoom_accessToken:" + memberUID, "newAccessToken", 1, TimeUnit.HOURS);
        verify(valueOperations, times(1)).set("zoom_refreshToken:" + memberUID, "newRefreshToken", 30, TimeUnit.DAYS);
    }

    @Test
    void refreshZoomToken_ShouldThrowException_WhenNoRefreshToken() {
        Long memberUID = 1L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> zoomService.refreshZoomToken(memberUID));
    }

    @Test
    void registerZoomParticipant_ShouldFail_WhenNoAccessToken() {
        Long meetingId = 123L;
        Long memberUID = 1L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn(null);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn(null);

        zoomService.registerZoomParticipant(meetingId, null, memberUID);

        verify(redisTemplate.opsForValue(), times(1)).get("zoom_refreshToken:" + memberUID);
    }

    @Test
    void createMeeting_ShouldReturnUnauthorized_WhenNoAccessToken() {
        Long memberUID = 1L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn(null);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn(null);

        ResponseEntity<?> response = zoomService.createMeeting(memberUID, null);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }

    @Test
    void isAlreadyRegistered_ShouldReturnTrue_WhenEmailIsRegistered() {
        Long meetingId = 123L;
        Long memberUID = 1L;
        String email = "test@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn("validToken");

        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn("mockedRefreshToken");

        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"registrants\":[{\"email\":\"test@example.com\"}]}"));

        boolean result = zoomService.isAlreadyRegistered(meetingId, email, memberUID);

        assertTrue(result);
    }


    @Test
    void isAlreadyRegistered_ShouldReturnFalse_WhenEmailNotRegistered() {
        Long meetingId = 123L;
        Long memberUID = 1L;
        String email = "test@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn("validToken");

        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn("mockedRefreshToken");

        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"registrants\":[]}"));

        boolean result = zoomService.isAlreadyRegistered(meetingId, email, memberUID);

        assertFalse(result);
    }


    @Test
    void getMemberUID_ShouldReturnUID_WhenValidMemberId() {
        String memberId = "testUser";
        when(memberRepository.getMemberIdById(memberId)).thenReturn(1L);

        Long result = zoomService.getMemberUID(memberId);

        assertNotNull(result);
        assertEquals(1L, result);
    }

    @Test
    void getMemberUID_ShouldReturnNull_WhenInvalidMemberId() {
        String memberId = "invalidUser";
        when(memberRepository.getMemberIdById(memberId)).thenReturn(null);

        Long result = zoomService.getMemberUID(memberId);

        assertNull(result);
    }
}
