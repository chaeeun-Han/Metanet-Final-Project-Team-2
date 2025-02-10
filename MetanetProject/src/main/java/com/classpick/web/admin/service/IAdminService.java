package com.classpick.web.admin.service;

import org.springframework.http.ResponseEntity;

import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.lecture.model.DeleteLectureRequest;
import com.classpick.web.member.model.DeleteMemberRequest;

public interface IAdminService {

	ResponseEntity<ResponseDto> getAllMembers();
	ResponseEntity<ResponseDto> deleteMembers(DeleteMemberRequest memberIds);
	ResponseEntity<ResponseDto> deleteAllMembers();
	ResponseEntity<ResponseDto> deleteLectures(DeleteLectureRequest lectureIds);
	ResponseEntity<ResponseDto> deleteAllLectures();
	
}
