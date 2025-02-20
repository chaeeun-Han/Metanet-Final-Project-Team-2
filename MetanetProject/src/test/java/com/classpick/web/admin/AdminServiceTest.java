package com.classpick.web.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.classpick.web.admin.service.AdminService;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.lecture.model.DeleteLectureRequest;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.member.model.DeleteMemberRequest;
import com.classpick.web.member.model.MemberResponse;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private IMemberRepository memberRepository;

    @Mock
    private ILectureRepository lectureRepository;

    @InjectMocks
    private AdminService adminService;

    private DeleteMemberRequest deleteMemberRequest;
    private DeleteLectureRequest deleteLectureRequest;

    @BeforeEach
    void setUp() {
        deleteMemberRequest = new DeleteMemberRequest();
        deleteMemberRequest.setMemberIds(List.of(1L, 2L));

        deleteLectureRequest = new DeleteLectureRequest();
        deleteLectureRequest.setLectureIds(List.of(10L, 20L, 30L));
    }

    @Test
    @DisplayName("회원 전체 조회 - 성공")
    void getAllMembers_Success() {
        List<MemberResponse> mockMemberList = List.of(
            new MemberResponse(1L, "010-1234-5678", "user1@email.com", "USER", "User One", null),
            new MemberResponse(2L, "010-9876-5432", "user2@email.com", "USER", "User Two", null)
        );

        when(memberRepository.getAllMembers()).thenReturn(mockMemberList);

        ResponseEntity<ResponseDto> response = adminService.getAllMembers();

        assertNotNull(response);
        assertEquals("SU", response.getBody().getCode());
        assertEquals("SUCCESS", response.getBody().getMessage().toUpperCase());
        assertEquals(2, ((List<?>) response.getBody().getData()).size());
    }

    @Test
    @DisplayName("회원 삭제 - 성공")
    void deleteMembers_Success() {
        doNothing().when(memberRepository).forceDeleteMember(anyLong());

        ResponseEntity<ResponseDto> response = adminService.deleteMembers(deleteMemberRequest);

        assertNotNull(response);
        assertEquals("SU", response.getBody().getCode());
        assertEquals("SUCCESS", response.getBody().getMessage().toUpperCase());
    }

    @Test
    @DisplayName("전체 회원 삭제 - 성공")
    void deleteAllMembers_Success() {
        doNothing().when(memberRepository).deleteAllMembers();

        ResponseEntity<ResponseDto> response = adminService.deleteAllMembers();

        assertNotNull(response);
        assertEquals("SU", response.getBody().getCode());
        assertEquals("SUCCESS", response.getBody().getMessage().toUpperCase());
    }

    @Test
    @DisplayName("강의 삭제 - 성공")
    void deleteLectures_Success() {
        doNothing().when(lectureRepository).forceDeleteLecture(anyLong());

        ResponseEntity<ResponseDto> response = adminService.deleteLectures(deleteLectureRequest);

        assertNotNull(response);
        assertEquals("SU", response.getBody().getCode());
        assertEquals("SUCCESS", response.getBody().getMessage().toUpperCase());
    }

    @Test
    @DisplayName("전체 강의 삭제 - 성공")
    void deleteAllLectures_Success() {
        doNothing().when(lectureRepository).deleteAllLectures();

        ResponseEntity<ResponseDto> response = adminService.deleteAllLectures();

        assertNotNull(response);
        assertEquals("SU", response.getBody().getCode());
        assertEquals("SUCCESS", response.getBody().getMessage().toUpperCase());
    }

    @Test
    @DisplayName("회원 전체 조회 - 실패 (DB 오류)")
    void getAllMembers_Fail_DatabaseError() {
        when(memberRepository.getAllMembers()).thenThrow(new RuntimeException("Database Error"));

        ResponseEntity<ResponseDto> response = adminService.getAllMembers();

        assertNotNull(response);
        assertEquals("DBE", response.getBody().getCode());
        assertEquals("database error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("회원 삭제 - 실패 (DB 오류)")
    void deleteMembers_Fail_DatabaseError() {
        doThrow(new RuntimeException("Database Error")).when(memberRepository).forceDeleteMember(anyLong());

        ResponseEntity<ResponseDto> response = adminService.deleteMembers(deleteMemberRequest);

        assertNotNull(response);
        assertEquals("DBE", response.getBody().getCode());
        assertEquals("database error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("전체 회원 삭제 - 실패 (DB 오류)")
    void deleteAllMembers_Fail_DatabaseError() {
        doThrow(new RuntimeException("Database Error")).when(memberRepository).deleteAllMembers();

        ResponseEntity<ResponseDto> response = adminService.deleteAllMembers();

        assertNotNull(response);
        assertEquals("DBE", response.getBody().getCode());
        assertEquals("database error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("강의 삭제 - 실패 (DB 오류)")
    void deleteLectures_Fail_DatabaseError() {
        doThrow(new RuntimeException("Database Error")).when(lectureRepository).forceDeleteLecture(anyLong());

        ResponseEntity<ResponseDto> response = adminService.deleteLectures(deleteLectureRequest);

        assertNotNull(response);
        assertEquals("DBE", response.getBody().getCode());
        assertEquals("database error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("전체 강의 삭제 - 실패 (DB 오류)")
    void deleteAllLectures_Fail_DatabaseError() {
        doThrow(new RuntimeException("Database Error")).when(lectureRepository).deleteAllLectures();

        ResponseEntity<ResponseDto> response = adminService.deleteAllLectures();

        assertNotNull(response);
        assertEquals("DBE", response.getBody().getCode());
        assertEquals("database error", response.getBody().getMessage());
    }
}
