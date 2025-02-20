package com.classpick.web.qna;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;

import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.lecture.model.Lecture;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.qna.dao.IQnaRepository;
import com.classpick.web.qna.model.Answer;
import com.classpick.web.qna.model.AnswerUpdateRequest;
import com.classpick.web.qna.model.Question;
import com.classpick.web.qna.model.QuestionDetail;
import com.classpick.web.qna.model.QuestionSummary;
import com.classpick.web.qna.model.QuestionUpdateRequest;
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

    @Test
    @DisplayName("질문 등록 - 성공")
    void registerQuestion_ShouldReturnSuccess() {
        Long lectureId = 1L;
        String memberId = "testUser";
        Long memberUID = 1L;
        String receiverUsername = "teacherUser";

        Lecture mockLecture = new Lecture();
        mockLecture.setMemberId(2L);

        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(lectureRepository.getLectureDetail(lectureId)).thenReturn(mockLecture);
        when(memberRepository.getIdByMemberId(mockLecture.getMemberId())).thenReturn(receiverUsername);

        doNothing().when(qnaRepository).insertQuestion(anyLong(), anyLong(), any(Question.class));

        ResponseEntity<ResponseDto> response = qnaService.registerQuestion(lectureId, memberId, question);

        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).insertQuestion(anyLong(), anyLong(), any(Question.class));
    }

    @Test
    @DisplayName("질문 내역 조회 - 성공")
    void getQuestionSummaries_ShouldReturnList() {
        Long lectureId = 1L;
        List<QuestionSummary> questions = Arrays.asList(new QuestionSummary(), new QuestionSummary());
        when(qnaRepository.getQuestionSummaries(lectureId)).thenReturn(questions);

        ResponseEntity<ResponseDto> response = qnaService.getQuestionSummaries(lectureId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("답변 등록 - 성공")
    void registerAnswer_ShouldReturnSuccess() {
        Long lectureId = 1L;
        Long questionId = 1L;
        String memberId = "testUser";
        Long memberUID = 1L;
        String receiverUsername = "questionOwner";

        Answer answer = new Answer();
        answer.setContent("This is an answer");

        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(2L);
        when(memberRepository.getIdByMemberId(2L)).thenReturn(receiverUsername);
        doNothing().when(qnaRepository).insertAnswer(anyLong(), anyLong(), anyLong(), any(Answer.class));

        ResponseEntity<ResponseDto> response = qnaService.registerAnswer(lectureId, questionId, memberId, answer);

        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).insertAnswer(anyLong(), anyLong(), anyLong(), any(Answer.class));
    }

    @Test
    @DisplayName("질문 수정 - 성공")
    void updateQuestion_ShouldReturnSuccess() {
        Long questionId = 1L;
        Long memberUID = 1L;
        String memberId = "testUser";

        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        doNothing().when(qnaRepository).updateQuestion(anyLong(), anyString(), anyString());

        ResponseEntity<ResponseDto> response = qnaService.updateQuestion(1L, questionId, memberId, questionUpdateRequest);

        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).updateQuestion(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("잘문 상세 조회 - 성공")
    void getQuestionDetails_ShouldReturnDetails() {
        Long questionId = 1L;
        QuestionDetail questionDetail = new QuestionDetail();
        questionDetail.setQuestionId(questionId);
        questionDetail.setAnswerDetails(Collections.emptyList());

        when(qnaRepository.getQuestionDetail(questionId)).thenReturn(questionDetail);

        ResponseEntity<ResponseDto> response = qnaService.getQuestionDetails(questionId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("질문 삭제 - 성공")
    void deleteQuestion_ShouldReturnSuccess() {
        Long questionId = 1L;
        Long memberUID = 1L;
        String memberId = "testUser";

        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        doNothing().when(qnaRepository).deleteQuestion(anyLong());

        ResponseEntity<ResponseDto> response = qnaService.deleteQuestion(memberId, questionId);

        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).deleteQuestion(anyLong());
    }

    @Test
    @DisplayName("답변 수정 - 성공")
    void updateAnswer_ShouldReturnSuccess() {
        Long answerId = 1L;
        Long memberUID = 1L;
        String memberId = "testUser";

        when(qnaRepository.getMemberIdByAnswerId(answerId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        doNothing().when(qnaRepository).updateAnswer(anyLong(), any(AnswerUpdateRequest.class));

        ResponseEntity<ResponseDto> response = qnaService.updateAnswer(answerId, memberId, answerUpdateRequest);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("답변 삭제 - 성공")
    void deleteAnswer_ShouldReturnSuccess() {
        Long answerId = 1L;
        Long memberUID = 1L;
        String memberId = "testUser";

        when(qnaRepository.getMemberIdByAnswerId(answerId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        doNothing().when(qnaRepository).deleteAnswer(anyLong());

        ResponseEntity<ResponseDto> response = qnaService.deleteAnswer(answerId, memberId);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("질문 수정 - 권한 없음(자기가 쓴 글 아님)")
    void updateQuestion_ShouldReturnNoPermission_WhenNotAuthor() {
        Long questionId = 1L;
        String memberId = "hackerUser";
        Long realAuthorUID = 1L;

        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(realAuthorUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(999L);

        ResponseEntity<ResponseDto> response = qnaService.updateQuestion(1L, questionId, memberId, questionUpdateRequest);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    @DisplayName("답변 삭제 - 권한 없음(자기가 쓴 글 아님)")
    void deleteAnswer_ShouldReturnNoPermission_WhenNotAuthor() {
        Long answerId = 1L;
        String memberId = "hackerUser";
        Long realAuthorUID = 1L;

        when(qnaRepository.getMemberIdByAnswerId(answerId)).thenReturn(realAuthorUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(999L);

        ResponseEntity<ResponseDto> response = qnaService.deleteAnswer(answerId, memberId);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    @DisplayName("질문 상세 조회 - DB 오류")
    void getQuestionDetails_ShouldReturnDatabaseError_WhenQuestionNotFound() {
        Long questionId = 999L;

        when(qnaRepository.getQuestionDetail(questionId)).thenReturn(null);

        ResponseEntity<ResponseDto> response = qnaService.getQuestionDetails(questionId);

        // Then
        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    @DisplayName("질문 등록 - DB 오류")
    void registerQuestion_ShouldHandleExceptionProperly() {
        Long lectureId = 1L;
        String memberId = "testUser";

        when(memberRepository.getMemberIdById(memberId)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<ResponseDto> response = qnaService.registerQuestion(lectureId, memberId, question);

        assertEquals(500, response.getStatusCode().value());
    }
    
    @Test
    @DisplayName("질문 등록 - 성공(사진 없음)")
    void registerQuestion_ShouldSaveQuestionWithoutImages() {
        Long lectureId = 1L;
        String memberId = "testUser";
        Long memberUID = 1L;

        Lecture mockLecture = new Lecture();
        mockLecture.setMemberId(2L);
        
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(lectureRepository.getLectureDetail(lectureId)).thenReturn(mockLecture);
        when(memberRepository.getIdByMemberId(mockLecture.getMemberId())).thenReturn("teacherUser");

        doNothing().when(qnaRepository).insertQuestion(anyLong(), anyLong(), any(Question.class));

        ResponseEntity<ResponseDto> response = qnaService.registerQuestion(lectureId, memberId, question);

        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).insertQuestion(anyLong(), anyLong(), any(Question.class));
    }

    @Test
    @DisplayName("질문 등록 - 성공(이미지 있음)")
    void registerQuestion_ShouldUploadImages_WhenImagesAreIncluded() {
        Long lectureId = 1L;
        String memberId = "testUser";
        Long memberUID = 1L;

        Lecture mockLecture = new Lecture();
        mockLecture.setMemberId(2L);

        MockMultipartFile file1 = new MockMultipartFile(
            "file", "image1.jpg", "image/jpeg", new byte[10]
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "image2.jpg", "image/jpeg", new byte[10]
        );

        question.setImages(List.of(file1, file2));

        
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(lectureRepository.getLectureDetail(lectureId)).thenReturn(mockLecture);
        when(memberRepository.getIdByMemberId(mockLecture.getMemberId())).thenReturn("teacherUser");

        doNothing().when(qnaRepository).insertQuestion(anyLong(), anyLong(), any(Question.class));
        when(s3FileUploader.uploadFiles(any(), anyString(), anyString(), anyLong()))
            .thenReturn(List.of("s3_url/image1.jpg", "s3_url/image2.jpg"));

        ResponseEntity<ResponseDto> response = qnaService.registerQuestion(lectureId, memberId, question);

        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).insertQuestion(anyLong(), anyLong(), any(Question.class));
        verify(qnaRepository, times(2)).insertQuestionImage(anyLong(), anyLong(), anyLong(), anyString());
    }
    
    @Test
    @DisplayName("답변 등록 - 자기글에 자기가 답변 달면 자기한테 알림 안 옴")
    void registerAnswer_ShouldNotSendNotification_WhenUserAnswersOwnQuestion() {
        Long lectureId = 1L;
        Long questionId = 1L;
        String memberId = "questionOwner";
        Long memberUID = 1L;

        Answer answer = new Answer();
        answer.setContent("This is an answer");

        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(memberUID);
        when(memberRepository.getIdByMemberId(memberUID)).thenReturn(memberId);
        doNothing().when(qnaRepository).insertAnswer(anyLong(), anyLong(), anyLong(), any(Answer.class));

        ResponseEntity<ResponseDto> response = qnaService.registerAnswer(lectureId, questionId, memberId, answer);

        assertEquals(200, response.getStatusCode().value());
        verify(messagingTemplate, times(0)).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("답변 등록 - 자기 글에 남의 답변달리면 작성자한테 알림 옴")
    void registerAnswer_ShouldSendNotification_WhenAnsweringOthersQuestion() {
        Long lectureId = 1L;
        Long questionId = 1L;
        String memberId = "answerer";
        Long memberUID = 2L;
        String questionOwnerUsername = "questionOwner";

        Answer answer = new Answer();
        answer.setContent("This is an answer");

        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(1L);
        when(memberRepository.getIdByMemberId(1L)).thenReturn(questionOwnerUsername);
        when(qnaRepository.getQuestionTitle(questionId)).thenReturn("Test Question");

        doNothing().when(qnaRepository).insertAnswer(anyLong(), anyLong(), anyLong(), any(Answer.class));

        ResponseEntity<ResponseDto> response = qnaService.registerAnswer(lectureId, questionId, memberId, answer);

        assertEquals(200, response.getStatusCode().value());
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq(questionOwnerUsername), anyString(), any());
    }

    @Test
    @DisplayName("질문 수정 - 이미지 포함")
    void updateQuestion_ShouldUpdateImages_WhenImagesAreProvided() {
        Long lectureId = 1L;
        Long questionId = 1L;
        String memberId = "testUser";
        Long memberUID = 1L;

        MockMultipartFile file1 = new MockMultipartFile(
            "file", "image1.jpg", "image/jpeg", new byte[10]
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "image2.jpg", "image/jpeg", new byte[10]
        );
        questionUpdateRequest.setImages(List.of(file1, file2));

        when(qnaRepository.getMemberIdByQuestionId(questionId)).thenReturn(memberUID);
        when(memberRepository.getMemberIdById(memberId)).thenReturn(memberUID);
        when(s3FileUploader.uploadFiles(any(), anyString(), anyString(), anyLong()))
            .thenReturn(List.of("s3_url/newImage1.jpg", "s3_url/newImage2.jpg"));

        doNothing().when(qnaRepository).updateQuestion(anyLong(), anyString(), anyString());
        doNothing().when(qnaRepository).deleteQuestionImages(anyLong());

        ResponseEntity<ResponseDto> response = qnaService.updateQuestion(lectureId, questionId, memberId, questionUpdateRequest);

        assertEquals(200, response.getStatusCode().value());
        verify(qnaRepository, times(1)).updateQuestion(anyLong(), anyString(), anyString());
        verify(qnaRepository, times(1)).deleteQuestionImages(questionId);
        verify(qnaRepository, times(2)).insertQuestionImage(eq(questionId), anyLong(), eq(lectureId), anyString());
    }

}
