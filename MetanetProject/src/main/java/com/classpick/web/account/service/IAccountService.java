package com.classpick.web.account.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.classpick.web.common.response.ResponseDto;

public interface IAccountService {
	ResponseEntity<ResponseDto> getLecture(String memberId);
	
	ResponseEntity<ResponseDto> insertCategory(String tags, String memberId);

	ResponseEntity<ResponseDto> updateProfile(String user, MultipartFile files);

	ResponseEntity<ResponseDto> getMyPage(String user);

	ResponseEntity<ResponseDto> getPaylog(String user);

	ResponseEntity<ResponseDto> getMyStudy(String user);

	ResponseEntity<ResponseDto> getMyLecture(String user);

}
