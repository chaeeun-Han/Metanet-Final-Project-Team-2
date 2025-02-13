package com.classpick.web.account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.classpick.web.account.dao.IAccountRepository;
import com.classpick.web.account.model.*;
import com.classpick.web.account.service.AccountService;
import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.member.model.Member;
import com.classpick.web.util.S3FileUploader;

@ExtendWith(MockitoExtension.class)
@Transactional
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private IMemberRepository memberRepository;

    @Mock
    private S3FileUploader s3FileUploader;

    private final String testUser = "asdf1234";
    private final Long testMemberId = 7L;

    @BeforeEach
    void setUp() {
        when(memberRepository.getMemberIdById(testUser)).thenReturn(testMemberId);
    }

    @Test
    @DisplayName("내 수강 강의 목록 조회 - 성공")
    void getLecture_Success() {
        List<AccountLecture> mockLectures = new ArrayList<>();
        when(accountRepository.getLecture(testMemberId)).thenReturn(mockLectures);

        ResponseEntity<ResponseDto> response = accountService.getLecture(testUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("프로필 조회 - 성공")
    void getMyPage_Success() {
        Member mockAccountMembers = new Member();
        when(accountRepository.getMyPage(testMemberId)).thenReturn(mockAccountMembers);

        ResponseEntity<ResponseDto> response = accountService.getMyPage(testUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("구매 내역 조회 - 성공")
    void getPaylog_Success() {
        List<Pay> mockPaylog = new ArrayList<>();
        when(accountRepository.getPaylog(testMemberId)).thenReturn(mockPaylog);

        ResponseEntity<ResponseDto> response = accountService.getPaylog(testUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("내 학습률 조회 - 성공")
    void getMyStudy_Success() {
        List<MyStudy> mockMyStudy = new ArrayList<>();
        when(accountRepository.getMyStudy(testMemberId)).thenReturn(mockMyStudy);

        ResponseEntity<ResponseDto> response = accountService.getMyStudy(testUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("강의 대시보드 조회 - 성공")
    void getMyLecture_Success() {
        TeacherLecture mockTeacherLecture = new TeacherLecture();
        when(accountRepository.getDueToLectures(testMemberId)).thenReturn(new ArrayList<>());
        when(accountRepository.getIngLectures(testMemberId)).thenReturn(new ArrayList<>());
        when(accountRepository.getEndLectures(testMemberId)).thenReturn(new ArrayList<>());

        ResponseEntity<ResponseDto> response = accountService.getMyLecture(testUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("프로필 업데이트 - 파일 없이 성공")
    void updateProfile_WithoutFile_Success() {
        UpdateMember updateMember = new UpdateMember();
        updateMember.setName("Updated Name");
        updateMember.setTags("AI,Cloud,Security");

        ResponseEntity<ResponseDto> response = accountService.updateProfile(testUser, updateMember);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("프로필 업데이트 - 파일 포함 성공")
    void updateProfile_WithFile_Success() {
        MockMultipartFile mockFile = new MockMultipartFile(
            "file", "test-image.jpg", "image/jpeg", "test image content".getBytes()
        );

        UpdateMember updateMember = new UpdateMember();
        updateMember.setName("Updated Name");
        updateMember.setTags("AI,Cloud,Security");
        updateMember.setFile(mockFile);

        when(s3FileUploader.uploadFile(any(), any(), any(), any())).thenReturn("https://s3-url.com/test-image.jpg");

        ResponseEntity<ResponseDto> response = accountService.updateProfile(testUser, updateMember);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("은행 정보 추가 - 성공")
    void addBank_Success() {
        ResponseEntity<ResponseDto> response = accountService.addBank("NH Bank", testUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("은행 정보 삭제 - 성공")
    void deleteBank_Success() {
        ResponseEntity<ResponseDto> response = accountService.deleteBank(testUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }

    @Test
    @DisplayName("은행 정보 수정 - 성공")
    void updateBank_Success() {
        ResponseEntity<ResponseDto> response = accountService.updateBank("KB Bank", testUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
    }
}
