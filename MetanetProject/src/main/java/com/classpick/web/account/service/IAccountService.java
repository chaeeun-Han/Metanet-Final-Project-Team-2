package com.classpick.web.account.service;

import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.classpick.web.account.model.UpdateMember;
import com.classpick.web.common.response.ResponseDto;

public interface IAccountService {
	ResponseEntity<ResponseDto> getLecture(String memberId);
	
	ResponseEntity<ResponseDto> insertCategory(String tags, String memberId);

	ResponseEntity<ResponseDto> getMyPage(String user);

	ResponseEntity<ResponseDto> getPaylog(String user);

	ResponseEntity<ResponseDto> getMyStudy(String user);

	ResponseEntity<ResponseDto> getMyLecture(String user);

	ResponseEntity<ResponseDto> updateBank(String bank,String user);

	ResponseEntity<ResponseDto> deleteBank(String user);

	ResponseEntity<ResponseDto> addBank(String bank, String user);

	ResponseEntity<ResponseDto> updateProfile(String user, UpdateMember member);

}
