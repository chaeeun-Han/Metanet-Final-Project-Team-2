package com.classpick.web.member.service;

import java.util.List;
import java.util.Optional;

import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;

import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.excel.model.MemberForExcel;
import com.classpick.web.jwt.model.JwtToken;
import com.classpick.web.member.model.Member;
import com.classpick.web.member.model.MemberResponse;

public interface IMemberService {
	// 인증 코드 이메일 발송
	ResponseEntity<ResponseDto> sendEmail(String type, String email) throws MessagingException;

	ResponseEntity<ResponseDto> sendEmail(String type, String email, Object data) throws MessagingException;

	// 코드 검증
	ResponseEntity<ResponseDto> verifyEmailCode(String email, String code);

	// 회원가입
	void insertMember(Member member);

	// 로그인
	JwtToken loginService(Member member);

	// id로 조회하기
	Optional<Member> findById(String id);

	boolean findByEmail(String email);

	void resetPw(String email, String password);

	String checkRefreshToken(String refreshToken);

	boolean revokeRefreshToken(String refreshToken);

	boolean checkRefreshTokenValidity(String refreshToken);

	boolean deleteMemberByToken(String refreshToken);

	ResponseEntity<ResponseDto> resetEmail(String user, String email);

	List<MemberForExcel> getMembersByLecture(Long lectureId);
}