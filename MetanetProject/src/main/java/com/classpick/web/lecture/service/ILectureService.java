package com.classpick.web.lecture.service;

import java.util.List;
import java.util.Map;

import com.classpick.web.lecture.model.Lecture;
import com.classpick.web.lecture.model.LectureFile;
import com.classpick.web.lecture.model.LectureId;
import com.classpick.web.lecture.model.LectureList;

public interface ILectureService {
    Map<String, List<Lecture>> getAllLectures();

    Lecture getLectureDetail(Long lectureId);

    boolean checkLikeLectures(Long memberId, Long lectureId);

    void insertLikeLectures(Long memberId, Long lectureId);

    void deleteLikeLectures(Long memberId, Long lectureId);

    void updateLikeLectures(Long memberId, Long lectureId, boolean exist);

    int lectureFileUpload(LectureFile lectureFile);

    void setRefundStatus(LectureId lectureId);

    Long registerLectures(Lecture lecture);

    void updateLectures(Lecture lecture);

    void deleteLectures(Long lectureId, Long memberId);

    Long getMemberIdById(String memberId);

    Long getLectureMaxId();

    List<LectureFile> getLectureFiles(Long lectureId);

    void insertLectureTags(Map<String, Object> params);

    void deleteLectureTags(Map<String, Object> params);

    List<Long> getExistingTags(Long lectureId);

    void updateLectureTags(Long lectureId, String tags);

    void buyLecture(Map<String, Long> params);

    Boolean checkCanRefund(Map<String, Long> params);

    void payRefund(Map<String, Long> params);

    Boolean checkBeforeBuyLecture(Map<String, Long> params);

    void insertLectureListByExcel(LectureList lectureList);
}
