package com.classpick.web.account.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.json.JsonWriter.Members;
import org.springframework.stereotype.Repository;

import com.classpick.web.account.model.AccountLecture;
import com.classpick.web.account.model.DueToLecture;
import com.classpick.web.account.model.EndLecture;
import com.classpick.web.account.model.IngLecture;
import com.classpick.web.account.model.MyStudy;
import com.classpick.web.account.model.MyStudyLectureList;
import com.classpick.web.account.model.Pay;

@Repository
@Mapper
public interface IAccountRepository {
	List<AccountLecture> getLecture(Long memberUID);	

	void insertCategory(Long memberUID, String tagName);

	void updateProfile(Long memberUID, String fileUrl);

	List<Members> getMyPage(Long memberUID);

	List<Pay> getPaylog(Long memberUID);

	double getAttendPercent(Long memberUID);

	List<MyStudy> getMyStudy(Long memberUID);

	List<MyStudyLectureList> getMyStudyLectureList(Long lecture_id, Long memberUID);

	List<DueToLecture> getDueToLectures(Long teacherId);

	List<IngLecture> getIngLectures(Long teacherId);

	List<EndLecture> getEndLectures(Long teacherId);

}
