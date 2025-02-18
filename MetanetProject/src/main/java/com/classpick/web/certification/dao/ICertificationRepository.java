package com.classpick.web.certification.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.classpick.web.certification.dto.Certification;

@Repository
@Mapper
public interface ICertificationRepository {

	String getNameByUser(Long memberUID);

	boolean getCourseable(Long memberUID, Long lecture_id);

	Certification getLecutre_title(String memberName, Long lecture_id);

}
