package com.classpick.web.qna;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.lecture.model.Lecture;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.qna.dao.IQnaRepository;
import com.classpick.web.qna.model.*;
import com.classpick.web.qna.service.QnaService;
import com.classpick.web.util.S3FileUploader;

@ExtendWith(MockitoExtension.class)
class QnaServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private IMemberRepository memberRepository;

    @Mock
    private IQnaRepository qnaRepository;

    @Mock
    private S3FileUploader s3FileUploader;

    @Mock
    private ILectureRepository lectureRepository;

    @InjectMocks
    private QnaService qnaService;

    private Question question;
    private Answer answer;
    private QuestionUpdateRequest questionUpdateRequest;
    private AnswerUpdateRequest answerUpdateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        question = new Question();
        question.setQuestionId(1L);
        question.setTitle("Test Question");
        question.setContent("This is a test question.");
        question.setDate(new Timestamp(System.currentTimeMillis()));

        answer = new Answer();
        answer.setContent("This is a test answer.");
        answer.setDate(new Timestamp(System.currentTimeMillis()));

        questionUpdateRequest = new QuestionUpdateRequest();
        questionUpdateRequest.setTitle("Updated Question");
        questionUpdateRequest.setContent("Updated Content");

        answerUpdateRequest = new AnswerUpdateRequest();
        answerUpdateRequest.setContent("Updated Answer");
    }

    /** 📌 질문 등록 - 성공 */
    @Test
    void registerQuestion_ShouldReturnSuccess() {
        // Given
        Long lectureId = 1L;
        String memberId = "testUser";
        Long memberUID = 1L;
        String receiverUsername = "teacherUser";

        Lecture mockLecture = new Lecture();
        mockLecture.setMemberId(2L);

        // Mock 설정
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(lectureRepository.getLectureDetail(lectureId)).thenReturn(mockLecture);
        when(memberRepository.getIdByMemberId(mockLecture.getMemberId())).thenReturn(receiverUsername);

        doNothing().when(qnaRepository).insertQuestion(anyLong(), anyLong(), any(Question.class));

        // When
        ResponseEntity<ResponseDto> response = qnaService.registerQuestion(lectureId, memberId, question);

        // Then
        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).insertQuestion(anyLong(), anyLong(), any(Question.class));
    }


    /** 📌 질문 목록 조회 - 성공 */
    @Test
    void getQuestionSummaries_ShouldReturnList() {
        // Given
        Long lectureId = 1L;
        List<QuestionSummary> questions = Arrays.asList(new QuestionSummary(), new QuestionSummary());
        when(qnaRepository.getQuestionSummaries(lectureId)).thenReturn(questions);

        // When
        ResponseEntity<ResponseDto> response = qnaService.getQuestionSummaries(lectureId);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    /** 📌 답변 등록 - 성공 */
    @Test
    void registerAnswer_ShouldReturnSuccess() {
        // Given
        Long lectureId = 1L;
        Long questionId = 1L;
        String memberId = "testUser";
        Long memberUID = 1L;
        String receiverUsername = "questionOwner";

        Answer answer = new Answer();
        answer.setContent("This is an answer");

        // Mock 설정
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(2L);
        when(memberRepository.getIdByMemberId(2L)).thenReturn(receiverUsername);
        doNothing().when(qnaRepository).insertAnswer(anyLong(), anyLong(), anyLong(), any(Answer.class));

        // When
        ResponseEntity<ResponseDto> response = qnaService.registerAnswer(lectureId, questionId, memberId, answer);

        // Then
        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).insertAnswer(anyLong(), anyLong(), anyLong(), any(Answer.class));
    }


    /** 📌 질문 수정 - 성공 */
    @Test
    void updateQuestion_ShouldReturnSuccess() {
        // Given
        Long questionId = 1L;
        Long memberUID = 1L;
        String memberId = "testUser";

        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        doNothing().when(qnaRepository).updateQuestion(anyLong(), anyString(), anyString());

        // When
        ResponseEntity<ResponseDto> response = qnaService.updateQuestion(1L, questionId, memberId, questionUpdateRequest);

        // Then
        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).updateQuestion(anyLong(), anyString(), anyString());
    }

    /** 📌 질문 상세 조회 - 성공 */
    @Test
    void getQuestionDetails_ShouldReturnDetails() {
        // Given
        Long questionId = 1L;
        QuestionDetail questionDetail = new QuestionDetail();
        questionDetail.setQuestionId(questionId);
        questionDetail.setAnswerDetails(Collections.emptyList());

        when(qnaRepository.getQuestionDetail(questionId)).thenReturn(questionDetail);

        // When
        ResponseEntity<ResponseDto> response = qnaService.getQuestionDetails(questionId);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    /** 📌 질문 삭제 - 성공 */
    @Test
    void deleteQuestion_ShouldReturnSuccess() {
        // Given
        Long questionId = 1L;
        Long memberUID = 1L;
        String memberId = "testUser";

        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        doNothing().when(qnaRepository).deleteQuestion(anyLong());

        // When
        ResponseEntity<ResponseDto> response = qnaService.deleteQuestion(memberId, questionId);

        // Then
        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).deleteQuestion(anyLong());
    }

    /** 📌 답변 수정 - 성공 */
    @Test
    void updateAnswer_ShouldReturnSuccess() {
        // Given
        Long answerId = 1L;
        Long memberUID = 1L;
        String memberId = "testUser";

        when(qnaRepository.getMemberIdByAnswerId(answerId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        doNothing().when(qnaRepository).updateAnswer(anyLong(), any(AnswerUpdateRequest.class));

        // When
        ResponseEntity<ResponseDto> response = qnaService.updateAnswer(answerId, memberId, answerUpdateRequest);

        // Then
        assertEquals(200, response.getStatusCode().value());
    }

    /** 📌 답변 삭제 - 성공 */
    @Test
    void deleteAnswer_ShouldReturnSuccess() {
        // Given
        Long answerId = 1L;
        Long memberUID = 1L;
        String memberId = "testUser";

        when(qnaRepository.getMemberIdByAnswerId(answerId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        doNothing().when(qnaRepository).deleteAnswer(anyLong());

        // When
        ResponseEntity<ResponseDto> response = qnaService.deleteAnswer(answerId, memberId);

        // Then
        assertEquals(200, response.getStatusCode().value());
    }

    /** 📌 질문 수정 - 실패 (작성자가 아닌 사용자가 수정 시도) */
    @Test
    void updateQuestion_ShouldReturnNoPermission_WhenNotAuthor() {
        // Given
        Long questionId = 1L;
        String memberId = "hackerUser";  // ❌ 다른 사용자가 수정 시도
        Long realAuthorUID = 1L;

        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(realAuthorUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(999L);  // ❌ 다른 사용자 ID

        // When
        ResponseEntity<ResponseDto> response = qnaService.updateQuestion(1L, questionId, memberId, questionUpdateRequest);

        // Then
        assertEquals(403, response.getStatusCode().value());  // ❌ 수정 권한 없음
    }

    /** 📌 답변 삭제 - 실패 (권한 없는 사용자) */
    @Test
    void deleteAnswer_ShouldReturnNoPermission_WhenNotAuthor() {
        // Given
        Long answerId = 1L;
        String memberId = "hackerUser"; // ❌ 작성자가 아님
        Long realAuthorUID = 1L;

        when(qnaRepository.getMemberIdByAnswerId(answerId)).thenReturn(realAuthorUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(999L); // ❌ 다른 사용자 ID

        // When
        ResponseEntity<ResponseDto> response = qnaService.deleteAnswer(answerId, memberId);

        // Then
        assertEquals(403, response.getStatusCode().value()); // ❌ 삭제 권한 없음
    }

    /** 📌 질문 상세 조회 - 실패 (존재하지 않는 질문) */
    @Test
    void getQuestionDetails_ShouldReturnDatabaseError_WhenQuestionNotFound() {
        // Given
        Long questionId = 999L; // 존재하지 않는 질문

        when(qnaRepository.getQuestionDetail(questionId)).thenReturn(null);

        // When
        ResponseEntity<ResponseDto> response = qnaService.getQuestionDetails(questionId);

        // Then
        assertEquals(500, response.getStatusCode().value()); // ❌ 데이터베이스 오류
    }

    /** 📌 질문 등록 - 실패 (예외 발생 시) */
    @Test
    void registerQuestion_ShouldHandleExceptionProperly() {
        // Given
        Long lectureId = 1L;
        String memberId = "testUser";

        when(memberRepository.getMemberIdById(memberId)).thenThrow(new RuntimeException("DB error"));

        // When
        ResponseEntity<ResponseDto> response = qnaService.registerQuestion(lectureId, memberId, question);

        // Then
        assertEquals(500, response.getStatusCode().value());  // ❌ 서버 오류 응답
    }
}
