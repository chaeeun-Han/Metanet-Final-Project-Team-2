package com.example.myapp.member.dao;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.example.myapp.member.model.Member;

@Repository
@Mapper
public interface IMemberRepository {

	void insertMember(Member member) ;
	Member selectMember(String userid);
	
	Optional<Member> findById(String id);

	int findByEmail(String email);
	
	String getUserIdByEmail(String email);
	
	void setNewPw(String email, String password);
	
	String getRoleById(String id);
	
	void deleteMember(String id);

	String getMemberIdById(String memberId);
	
	void resetEmail(String email, String memberUID);
	
}