package com.classpick.web.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.classpick.web.account.dao.IAccountRepository;
import com.classpick.web.account.model.AccountLecture;
import com.classpick.web.account.model.MyStudy;
import com.classpick.web.account.model.TeacherLecture;
import com.classpick.web.account.model.UpdateMember;
import com.classpick.web.account.service.AccountService;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.member.model.Member;
import com.classpick.web.util.S3FileUploader;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private IMemberRepository memberRepository;

    @Mock
    private S3FileUploader s3FileUploader;

    private final String testUser = "testUser";
    private final Long testMemberId = 1L;

    @Test
    @DisplayName("강의 조회 - 성공")
    void getLecture_Success() {
        List<AccountLecture> mockLectures = List.of(new AccountLecture("IT", "Java Basics", "profile.jpg", true));
        when(accountRepository.getLecture(testMemberId)).thenReturn(mockLectures);
        when(memberRepository.getMemberIdById(testUser)).thenReturn(testMemberId);
        ResponseEntity<ResponseDto> response = accountService.getLecture(testUser);

        assertEquals(200, response.getStatusCode().value());
        assertFalse(((List<?>) response.getBody().getData()).isEmpty());
    }

    @Test
    @DisplayName("강의 조회 - 강의 없음")
    void getLecture_Empty() {
        when(accountRepository.getLecture(testMemberId)).thenReturn(new ArrayList<>());
        when(memberRepository.getMemberIdById(testUser)).thenReturn(testMemberId);
        ResponseEntity<ResponseDto> response = accountService.getLecture(testUser);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(((List<?>) response.getBody().getData()).isEmpty());
    }
    
    @Test
    @DisplayName("내 수강 강의 조회 - 강의 없음")
    void getMyStudy_MyStudyLectureListEmpty() {
        MyStudy myStudy = new MyStudy();
        myStudy.setLectureId(1L);

        List<MyStudy> myStudies = List.of(myStudy);
        when(memberRepository.getMemberIdById(testUser)).thenReturn(testMemberId);
        when(accountRepository.getMyStudy(testMemberId)).thenReturn(myStudies);
        when(accountRepository.getMyStudyLectureList(anyLong(), eq(testMemberId)))
            .thenReturn(new ArrayList<>());

        ResponseEntity<ResponseDto> response = accountService.getMyStudy(testUser);

        assertEquals(200, response.getStatusCode().value());

        List<MyStudy> responseData = (List<MyStudy>) response.getBody().getData();
        assertFalse(responseData.isEmpty());
        assertTrue(responseData.get(0).getMyStudyLectureList().isEmpty());
    }

    @Test
    @DisplayName("마이페이지 조회 - 성공")
    void getMyPage_Success() {
    	when(accountRepository.getMyPage(testMemberId)).thenReturn(new Member());
    	when(memberRepository.getMemberIdById(testUser)).thenReturn(testMemberId);
        ResponseEntity<ResponseDto> response = accountService.getMyPage(testUser);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("마이페이지 조회 - DB 오류")
    void getMyPage_DatabaseError() {
        when(accountRepository.getMyPage(testMemberId)).thenThrow(new RuntimeException("DB Error"));
        ResponseEntity<ResponseDto> response = accountService.getMyPage(testUser);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("프로필 수정 - 성공(이미지 없이)")
    void updateProfile_WithoutFile_Success() {
        UpdateMember updateMember = new UpdateMember();
        updateMember.setName("Updated Name");
        updateMember.setTags("AI, Cloud, Security");

        ResponseEntity<ResponseDto> response = accountService.updateProfile(testUser, updateMember);
        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    @DisplayName("프로필 수정 - 태그 없음")
    void updateProfile_EmptyTags_ShouldReturn200() {
        UpdateMember updateMember = new UpdateMember();
        updateMember.setName("Updated Name");
        updateMember.setTags("");

        ResponseEntity<ResponseDto> response = accountService.updateProfile(testUser, updateMember);
        assertEquals(200, response.getStatusCode().value());
    }
    
    @Test
    @DisplayName("프로필 수정 - 태그 있음")
    void updateProfile_WithExistingCategories_ShouldDeleteAndInsertCategories() {
        UpdateMember updateMember = new UpdateMember();
        updateMember.setName("Updated Name");
        updateMember.setTags("AI, Cloud, Security");
        when(memberRepository.getMemberIdById(testUser)).thenReturn(testMemberId);
        when(accountRepository.selectCount(testMemberId)).thenReturn(1);

        ResponseEntity<ResponseDto> response = accountService.updateProfile(testUser, updateMember);

        assertEquals(200, response.getStatusCode().value());

        verify(accountRepository, times(1)).deleteCategory(testMemberId);
    }

    @Test
    @DisplayName("은행 계좌 수정 - 성공")
    void updateBank_Success() {
        String bankName = "KB Bank";
        ResponseEntity<ResponseDto> response = accountService.updateBank(bankName, testUser);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("은행 계좌 수정 - DB 오류")
    void updateBank_DatabaseError() {
        String bankName = "KB Bank";
        doThrow(new RuntimeException("DB Error")).when(accountRepository).updateBank(eq(bankName), eq(testMemberId));

        ResponseEntity<ResponseDto> response = accountService.updateBank(bankName, testUser);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("은행 계좌 추가 - 성공")
    void addBank_Success() {
        String bankName = "KB Bank";
        ResponseEntity<ResponseDto> response = accountService.addBank(bankName, testUser);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("은행 계좌 추가 - DB 오류")
    void addBank_DatabaseError() {
        String bankName = "KB Bank";
        doThrow(new RuntimeException("DB Error")).when(accountRepository).addBank(eq(bankName), eq(testMemberId));

        ResponseEntity<ResponseDto> response = accountService.addBank(bankName, testUser);
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("은행 계좌 삭제 - 성공")
    void deleteBank_Success() {
        ResponseEntity<ResponseDto> response = accountService.deleteBank(testUser);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("은행 계좌 삭제 - DB 오류")
    void deleteBank_DatabaseError() {
        doThrow(new RuntimeException("DB Error")).when(accountRepository).deleteBank(eq(testMemberId));

        ResponseEntity<ResponseDto> response = accountService.deleteBank(testUser);
        assertEquals(500, response.getStatusCode().value());
    }
    
    @Test
    @DisplayName("선생님 강의 조회 - null")
    void getTeacherLectures_AllNull_ShouldReturnEmptyLists() {
        when(accountRepository.getDueToLectures(testMemberId)).thenReturn(null);
        when(accountRepository.getIngLectures(testMemberId)).thenReturn(null);
        when(accountRepository.getEndLectures(testMemberId)).thenReturn(null);

        TeacherLecture result = accountService.getTeacherLectures(testMemberId);

        assertNotNull(result);
        assertNotNull(result.getDueToLecture());
        assertNotNull(result.getIngLecture());
        assertNotNull(result.getEndLecture());

        assertTrue(result.getDueToLecture().isEmpty());
        assertTrue(result.getIngLecture().isEmpty());
        assertTrue(result.getEndLecture().isEmpty());
    }

    @Test
    @DisplayName("선생님 강의 조회 - empty")
    void getTeacherLectures_AllEmptyLists_ShouldReturnEmptyLists() {
        when(accountRepository.getDueToLectures(testMemberId)).thenReturn(new ArrayList<>());
        when(accountRepository.getIngLectures(testMemberId)).thenReturn(new ArrayList<>());
        when(accountRepository.getEndLectures(testMemberId)).thenReturn(new ArrayList<>());

        TeacherLecture result = accountService.getTeacherLectures(testMemberId);

        assertNotNull(result);
        assertNotNull(result.getDueToLecture());
        assertNotNull(result.getIngLecture());
        assertNotNull(result.getEndLecture());

        assertTrue(result.getDueToLecture().isEmpty());
        assertTrue(result.getIngLecture().isEmpty());
        assertTrue(result.getEndLecture().isEmpty());
    }

    @Test
    @DisplayName("오류")
    void unexpectedErrorHandling() {
        when(accountRepository.getLecture(testMemberId)).thenThrow(new RuntimeException("Unexpected Error"));

        ResponseEntity<ResponseDto> response = accountService.getLecture(testUser);
        assertEquals(500, response.getStatusCode().value());
    }
    
    @Test
    @DisplayName("강의 조회 - DB 오류")
    void getLecture_ShouldReturnDatabaseError_WhenRepositoryThrowsException() {
        when(accountRepository.getLecture(anyLong())).thenThrow(new RuntimeException("DB 오류"));

        ResponseEntity<ResponseDto> response = accountService.getLecture("testUser");

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("강의 조회 - empty")
    void getLecture_ShouldReturnEmptyList_WhenNoLecturesFound() {
        when(accountRepository.getLecture(anyLong())).thenReturn(new ArrayList<>());

        ResponseEntity<ResponseDto> response = accountService.getLecture("testUser");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(((List<?>) response.getBody().getData()).isEmpty());
    }

    @Test
    @DisplayName("구매 내역 조회 - empty")
    void getPaylog_ShouldReturnEmptyList_WhenNoPaymentsFound() {
        when(accountRepository.getPaylog(anyLong())).thenReturn(new ArrayList<>());
        
        ResponseEntity<ResponseDto> response = accountService.getPaylog("testUser");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(((List<?>) response.getBody().getData()).isEmpty());
    }

    @Test
    @DisplayName("구매 내역 조회 - DB 오류")
    void getPaylog_ShouldReturnDatabaseError_WhenRepositoryThrowsException() {
        when(accountRepository.getPaylog(anyLong())).thenThrow(new RuntimeException("DB 오류"));

        ResponseEntity<ResponseDto> response = accountService.getPaylog("testUser");

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("내 학습 조회 - empty")
    void getMyStudy_ShouldReturnEmptyList_WhenNoStudiesFound() {
        when(accountRepository.getMyStudy(anyLong())).thenReturn(new ArrayList<>());

        ResponseEntity<ResponseDto> response = accountService.getMyStudy("testUser");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(((List<?>) response.getBody().getData()).isEmpty());
    }

    @Test
    @DisplayName("내 학습 조회 - 학습 정보는 있지만 강의 목록 없음")
    void getMyStudy_ShouldReturnEmptyLectureLists_WhenNoLectureListsFound() {
        MyStudy myStudy = new MyStudy();
        myStudy.setLectureId(1L);
        List<MyStudy> myStudies = List.of(myStudy);

        when(accountRepository.getMyStudy(anyLong())).thenReturn(myStudies);
        when(accountRepository.getMyStudyLectureList(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        ResponseEntity<ResponseDto> response = accountService.getMyStudy("testUser");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(((List<MyStudy>) response.getBody().getData()).get(0).getMyStudyLectureList().isEmpty());
    }

    @Test
    @DisplayName("내 학습 조회 - 모든 강의 목록 없음")
    void getMyLecture_ShouldReturnEmptyLists_WhenNoLecturesFound() {
        when(accountRepository.getDueToLectures(anyLong())).thenReturn(new ArrayList<>());
        when(accountRepository.getIngLectures(anyLong())).thenReturn(new ArrayList<>());
        when(accountRepository.getEndLectures(anyLong())).thenReturn(new ArrayList<>());

        ResponseEntity<ResponseDto> response = accountService.getMyLecture("testUser");

        assertEquals(200, response.getStatusCode().value());
        TeacherLecture data = (TeacherLecture) response.getBody().getData();
        assertTrue(data.getDueToLecture().isEmpty());
        assertTrue(data.getIngLecture().isEmpty());
        assertTrue(data.getEndLecture().isEmpty());
    }

    @Test
    @DisplayName("선생님 강의 조회 - null")
    void getTeacherLectures_ShouldHandleNullLists() {
        when(accountRepository.getDueToLectures(anyLong())).thenReturn(null);
        when(accountRepository.getIngLectures(anyLong())).thenReturn(null);
        when(accountRepository.getEndLectures(anyLong())).thenReturn(null);

        TeacherLecture result = accountService.getTeacherLectures(testMemberId);

        assertNotNull(result);
        assertNotNull(result.getDueToLecture());
        assertNotNull(result.getIngLecture());
        assertNotNull(result.getEndLecture());

        assertTrue(result.getDueToLecture().isEmpty());
        assertTrue(result.getIngLecture().isEmpty());
        assertTrue(result.getEndLecture().isEmpty());
    }

    @Test
    @DisplayName("태그 추가 - 성공")
    void insertCategory_ShouldInsertCategories_WhenValidTagsProvided() {
        when(memberRepository.getMemberIdById(testUser)).thenReturn(testMemberId);

        ResponseEntity<ResponseDto> response = accountService.insertCategory("AI, Cloud, Security", testUser);

        assertEquals(200, response.getStatusCode().value());

        verify(accountRepository, times(1)).insertCategory(testMemberId, "AI");
        verify(accountRepository, times(1)).insertCategory(testMemberId, "Cloud");
        verify(accountRepository, times(1)).insertCategory(testMemberId, "Security");
    }

}
