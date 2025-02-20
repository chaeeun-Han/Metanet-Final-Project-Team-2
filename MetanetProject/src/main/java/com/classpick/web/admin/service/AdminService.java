package com.classpick.web.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classpick.web.admin.model.AdminDashboard;
import com.classpick.web.admin.model.LectureDashboard;
import com.classpick.web.admin.model.MemDashboard;
import com.classpick.web.admin.model.PercentDashboard;
import com.classpick.web.admin.model.StudentTeacherDashboard;
import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.common.response.ResponseMessage;
import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.lecture.model.DeleteLectureRequest;
import com.classpick.web.lecture.model.Lecture;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.member.model.DeleteMemberRequest;
import com.classpick.web.member.model.MemberResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminService implements IAdminService{
	
	@Autowired
	IMemberRepository memberRepository;
	
	@Autowired
	ILectureRepository lectureRepository;
	
	// 회원 전체 조회
    @Override
    public ResponseEntity<ResponseDto> getAllMembers() {
       try {
          List<MemberResponse> members = memberRepository.getAllMembers();
          ResponseDto<List<MemberResponse>> responseBody = new ResponseDto<List<MemberResponse>>(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, members);
          return ResponseEntity.ok(responseBody);
       } catch(Exception e) {
          e.printStackTrace();
          return ResponseDto.databaseError();
       }
    }
	// 회원 삭제
	@Override
	@Transactional
	public ResponseEntity<ResponseDto> deleteMembers(DeleteMemberRequest memberIds) {
		try {
			for (Long memberId : memberIds.getMemberIds()) {
				memberRepository.forceDeleteMember(memberId);
			}
		} catch(Exception e) {
			e.printStackTrace();
			return ResponseDto.databaseError();
		}		
		return ResponseDto.success();
	}
	
	// 전체 회원 삭제
	@Override
	@Transactional
	public ResponseEntity<ResponseDto> deleteAllMembers() {
		try {
			memberRepository.deleteAllMembers();
		} catch(Exception e) {
			e.printStackTrace();
			return ResponseDto.databaseError();
		}		
		return ResponseDto.success();
	}
	
	// 강의 삭제
	@Override
	@Transactional
	public ResponseEntity<ResponseDto> deleteLectures(DeleteLectureRequest lectureIds) {
		try {
			for (Long lectureId : lectureIds.getLectureIds()) {
				lectureRepository.forceDeleteLecture(lectureId);
			}
		} catch(Exception e) {
			e.printStackTrace();
			return ResponseDto.databaseError();
		}		
		return ResponseDto.success();
	}
	
	// 전체 강의 삭제
	@Override
	@Transactional
	public ResponseEntity<ResponseDto> deleteAllLectures() {
		try {
				lectureRepository.deleteAllLectures();
		} catch(Exception e) {
			e.printStackTrace();
			return ResponseDto.databaseError();
		}		
		return ResponseDto.success();
	}

	@Override
	public ResponseEntity<ResponseDto> getAllLectures() {
		List<Lecture> lectures = lectureRepository.getAllLecture();
		ResponseDto<List<Lecture>> responseBody = new ResponseDto<List<Lecture>>(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, lectures);
		return ResponseEntity.ok(responseBody);
	}

	@Override
	public ResponseEntity<ResponseDto> getDashboard() {
		
		List<MemDashboard> memDashboard = memberRepository.getMemDashboard();
		List<LectureDashboard> lectureDashboard = lectureRepository.getLectureDashboard();
		List<PercentDashboard> percentDashboard = lectureRepository.getPercentDashboard();
		List<StudentTeacherDashboard> studentteacherDashboard = memberRepository.getStudentTeacherDashboard();
		AdminDashboard adminDashboard = new AdminDashboard();
		adminDashboard.setMemDashboard(memDashboard);
		adminDashboard.setLectureDashboard(lectureDashboard);
		adminDashboard.setPercentDashboard(percentDashboard);
		adminDashboard.setStudentteacherDashboard(studentteacherDashboard);
		
		ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, adminDashboard);
		
		return ResponseEntity.ok(responseBody);
	}
	
	


}
