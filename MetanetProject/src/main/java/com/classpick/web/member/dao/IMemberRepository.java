package com.classpick.web.member.dao;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.classpick.web.account.model.EndLecture;
import com.classpick.web.admin.model.LectureDashboard;
import com.classpick.web.admin.model.MemDashboard;
import com.classpick.web.admin.model.PercentDashboard;
import com.classpick.web.admin.model.StudentTeacherDashboard;
import com.classpick.web.excel.model.MemberForExcel;
import com.classpick.web.member.model.Member;
import com.classpick.web.member.model.MemberResponse;

@Repository
@Mapper
public interface IMemberRepository {

	void insertMember(Member member);

	Member selectMember(String userid);

	Optional<Member> findById(String id);

	int findByEmail(String email);

	String getUserIdByEmail(String email);

	void setNewPw(String email, String password);

	String getRoleById(String id);

	void deleteMember(String id);

	void resetEmail(String email, Long memberUID);

	Long getMemberIdById(String memberId);

	List<MemberResponse> getAllMembers();

	void forceDeleteMember(Long memberId);

	void deleteAllMembers();

	String getIdByMemberId(Long memberId);

	List<MemberForExcel> getMembersByLecture(Long lectureId);
	
	String getAttendIdById(Long memberUID);

	Member getToolMember(Long usmemberUIDer);

	Member findByUserId(String user);

	int isEmailDuplicated(String email);

	Member getAllMembers(Long memberUID);

	List<MemDashboard> getMemDashboard();

	List<StudentTeacherDashboard> getStudentTeacherDashboard();		
	
}