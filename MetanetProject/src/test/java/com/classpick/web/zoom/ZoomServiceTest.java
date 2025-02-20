package com.classpick.web.zoom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.lecture.model.LectureReminderDto;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.zoom.dao.IZoomRepository;
import com.classpick.web.zoom.model.ZoomDate;
import com.classpick.web.zoom.model.ZoomMeetingRequest;
import com.classpick.web.zoom.model.ZoomMeetingResponse;
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

    @InjectMocks
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
    void createMeeting_ShouldSucceed_WhenValidAccessToken() {
        Long memberUID = 1L;
        String email = "host@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn("validToken");
        when(memberRepository.getAttendIdById(memberUID)).thenReturn(email);

        ZoomMeetingRequest mockRequest = new ZoomMeetingRequest();
        List<ZoomDate> zoomDates = new ArrayList<>();

        ZoomDate mockZoomDate = new ZoomDate();
        mockZoomDate.setLectureListId(123L);
        mockZoomDate.setDate(LocalDate.now());
        mockZoomDate.setStartTime(LocalTime.of(9, 0));
        mockZoomDate.setEndTime(LocalTime.of(10, 0));
        zoomDates.add(mockZoomDate);

        mockRequest.setZoomDates(zoomDates);

        ZoomMeetingResponse mockZoomMeetingResponse = new ZoomMeetingResponse();
        mockZoomMeetingResponse.setLectureListId(123L);

        String zoomApiUrl = "https://api.zoom.us/v2/users/" + email + "/meetings";

        when(restTemplate.exchange(eq(zoomApiUrl), eq(HttpMethod.POST), any(), eq(ZoomMeetingResponse.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(mockZoomMeetingResponse));

        doNothing().when(lectureRepository).updateMeetingInfo(any());

        ResponseEntity<ResponseDto> response = zoomService.createMeeting(memberUID, mockRequest);

        verify(restTemplate, times(1)).exchange(eq(zoomApiUrl), eq(HttpMethod.POST), any(), eq(ZoomMeetingResponse.class));

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

        verify(lectureRepository, times(1)).updateMeetingInfo(any());

    }


    @Test
    void createMeeting_ShouldReturnUnauthorized_WhenInvalidAccessToken() {
        Long memberUID = 1L;
        String email = "host@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn("invalidToken");
        when(memberRepository.getAttendIdById(memberUID)).thenReturn(email);

        ZoomMeetingRequest mockRequest = new ZoomMeetingRequest();
        List<ZoomDate> zoomDates = new ArrayList<>();
        ZoomDate mockZoomDate = new ZoomDate();
        mockZoomDate.setLectureListId(123L);
        mockZoomDate.setDate(LocalDate.now());
        mockZoomDate.setStartTime(LocalTime.of(9, 0));
        mockZoomDate.setEndTime(LocalTime.of(10, 0));
        zoomDates.add(mockZoomDate);
        mockRequest.setZoomDates(zoomDates);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ZoomMeetingResponse.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        ResponseEntity<ResponseDto> response = zoomService.createMeeting(memberUID, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }

    @Test
    void createMeeting_ShouldReturnBadRequest_WhenZoomApiFails() {
        Long memberUID = 1L;
        String email = "host@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn("validToken");
        when(memberRepository.getAttendIdById(memberUID)).thenReturn(email);

        ZoomMeetingRequest mockRequest = new ZoomMeetingRequest();
        List<ZoomDate> zoomDates = new ArrayList<>();
        ZoomDate mockZoomDate = new ZoomDate();
        mockZoomDate.setLectureListId(123L);
        mockZoomDate.setDate(LocalDate.now());
        mockZoomDate.setStartTime(LocalTime.of(9, 0));
        mockZoomDate.setEndTime(LocalTime.of(10, 0));
        zoomDates.add(mockZoomDate);
        mockRequest.setZoomDates(zoomDates);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ZoomMeetingResponse.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        ResponseEntity<ResponseDto> response = zoomService.createMeeting(memberUID, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    void createMeeting_ShouldReturnServerError_WhenDBUpdateFails() {
        Long memberUID = 1L;
        String email = "host@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn("validToken");
        when(memberRepository.getAttendIdById(memberUID)).thenReturn(email);

        ZoomMeetingRequest mockRequest = new ZoomMeetingRequest();
        List<ZoomDate> zoomDates = new ArrayList<>();
        ZoomDate mockZoomDate = new ZoomDate();
        mockZoomDate.setLectureListId(123L);
        mockZoomDate.setDate(LocalDate.now());
        mockZoomDate.setStartTime(LocalTime.of(9, 0));
        mockZoomDate.setEndTime(LocalTime.of(10, 0));
        zoomDates.add(mockZoomDate);
        mockRequest.setZoomDates(zoomDates);

        ZoomMeetingResponse mockZoomMeetingResponse = new ZoomMeetingResponse();
        mockZoomMeetingResponse.setLectureListId(123L);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ZoomMeetingResponse.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(mockZoomMeetingResponse));

        doThrow(new RuntimeException("DB 업데이트 오류")).when(lectureRepository).updateMeetingInfo(any());

        ResponseEntity<ResponseDto> response = zoomService.createMeeting(memberUID, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }

    @Test
    void createMeeting_ShouldReturnNotFound_WhenZoomUserDoesNotExist() {
        Long memberUID = 1L;
        String email = "invalid-user@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn("validToken");
        when(memberRepository.getAttendIdById(memberUID)).thenReturn(email);

        ZoomMeetingRequest mockRequest = new ZoomMeetingRequest();
        List<ZoomDate> zoomDates = new ArrayList<>();
        ZoomDate mockZoomDate = new ZoomDate();
        mockZoomDate.setLectureListId(123L);
        mockZoomDate.setDate(LocalDate.now());
        mockZoomDate.setStartTime(LocalTime.of(9, 0));
        mockZoomDate.setEndTime(LocalTime.of(10, 0));
        zoomDates.add(mockZoomDate);
        mockRequest.setZoomDates(zoomDates);

        String zoomApiUrl = "https://api.zoom.us/v2/users/" + email + "/meetings";

        when(restTemplate.exchange(eq(zoomApiUrl), eq(HttpMethod.POST), any(), eq(ZoomMeetingResponse.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "User does not exist"));

        ResponseEntity<ResponseDto> response = zoomService.createMeeting(memberUID, mockRequest);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
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

    @Test
    void registerZoomParticipant_ShouldRefreshToken_WhenAccessTokenIsNull() {
        Long meetingId = 123L;
        Long memberUID = 1L;

        LectureReminderDto participant = new LectureReminderDto(
            "test@example.com",
            "Spring Boot Lecture",
            "10:00 AM",
            "https://zoom.us/j/12345",
            "test@example.com",
            meetingId,
            999L
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn(null);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn("mockedRefreshToken");

        String mockApiResponse = "{\"access_token\":\"newAccessToken\",\"refresh_token\":\"newRefreshToken\"}";
        ResponseEntity<String> mockResponse = ResponseEntity.ok(mockApiResponse);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(mockResponse);

        zoomService.registerZoomParticipant(meetingId, participant, memberUID);

        verify(valueOperations, times(2)).get("zoom_refreshToken:" + memberUID);
    }


    @Test
    void registerZoomParticipant_ShouldReturn_WhenNoRefreshToken() {
        Long meetingId = 123L;
        Long memberUID = 1L;

        LectureReminderDto participant = new LectureReminderDto(
            "test@example.com",
            "Spring Boot Lecture",
            "10:00 AM",
            "https://zoom.us/j/12345",
            "test@example.com",
            meetingId,
            999L
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn(null);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn(null);

        zoomService.registerZoomParticipant(meetingId, participant, memberUID);

        verify(valueOperations, times(1)).get("zoom_refreshToken:" + memberUID);
    }



    @Test
    void registerZoomParticipant_ShouldFail_WhenZoomApiFails() {
        Long meetingId = 123L;
        Long memberUID = 1L;

        LectureReminderDto participant = new LectureReminderDto(
            "test@example.com",
            "Spring Boot Lecture",
            "10:00 AM",
            "https://zoom.us/j/12345",
            "test@example.com",
            meetingId,
            999L
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn("mockedRefreshToken");

        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn("validToken");

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        zoomService.registerZoomParticipant(meetingId, participant, memberUID);

        verify(valueOperations, times(1)).get("zoom_refreshToken:" + memberUID);
        verify(valueOperations, times(1)).get("zoom_accessToken:" + memberUID);
    }

    @Test
    void registerZoomParticipant_ShouldReturn_WhenAccessTokenAndRefreshTokenAreNull() {
        Long meetingId = 123L;
        Long memberUID = 1L;

        LectureReminderDto participant = new LectureReminderDto(
            "test@example.com",
            "Spring Boot Lecture",
            "10:00 AM",
            "https://zoom.us/j/12345",
            "test@example.com",
            meetingId,
            999L
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn(null);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn(null);

        zoomService.registerZoomParticipant(meetingId, participant, memberUID);

        verify(valueOperations, times(1)).get("zoom_refreshToken:" + memberUID);
    }

    @Test
    void createMeeting_ShouldReturnUnauthorized_WhenAccessTokenAndRefreshTokenAreNull() {
        Long memberUID = 1L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn(null);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn(null);

        ResponseEntity<ResponseDto> response = zoomService.createMeeting(memberUID, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void isAlreadyRegistered_ShouldReturnFalse_WhenAccessTokenAndRefreshTokenAreNull() {
        Long meetingId = 123L;
        Long memberUID = 1L;
        String email = "test@example.com";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("zoom_accessToken:" + memberUID)).thenReturn(null);
        when(valueOperations.get("zoom_refreshToken:" + memberUID)).thenReturn(null);

        boolean result = zoomService.isAlreadyRegistered(meetingId, email, memberUID);

        assertFalse(result);
    }



}
