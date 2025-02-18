package com.classpick.web.qna.service;

import org.springframework.http.ResponseEntity;

import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.qna.model.Answer;
import com.classpick.web.qna.model.AnswerUpdateRequest;
import com.classpick.web.qna.model.Question;
import com.classpick.web.qna.model.QuestionUpdateRequest;

public interface IQnaService {
	ResponseEntity<ResponseDto> registerQuestion(Long lectureId, String memberId, Question question);
	ResponseEntity<ResponseDto> registerAnswer(Long lectureId, Long questionId, String memberId, Answer answer);
	ResponseEntity<ResponseDto> getQuestionSummaries(Long lectureId);
	ResponseEntity<ResponseDto> updateQuestion(Long lectureId, Long questionId, String memberId, QuestionUpdateRequest questionUpdateRequest);
	ResponseEntity<ResponseDto> getQuestionDetails(Long questionId);
	ResponseEntity<ResponseDto> deleteQuestion(String memberId, Long questionId);
	ResponseEntity<ResponseDto> updateAnswer(Long answerId, String user, AnswerUpdateRequest answerUpdateRequest);
	ResponseEntity<ResponseDto> deleteAnswer(Long answerId, String user);
}
