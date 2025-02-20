package com.classpick.web.lecture.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.common.response.ResponseMessage;
import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.lecture.model.Lecture;
import com.classpick.web.lecture.model.LectureFile;
import com.classpick.web.lecture.model.LectureId;
import com.classpick.web.lecture.model.LectureList;
import com.classpick.web.lecture.model.Tag;

@Service
public class LectureService implements ILectureService {

    @Autowired
    ILectureRepository lectureDao;

    @Override
    public Map<String, List<Lecture>> getAllLectures() {

        Map<String, List<Lecture>> lectures = new HashMap<String, List<Lecture>>();

        lectures.put("getAll", lectureDao.getAllLectures());
        lectures.put("getRankByDeadDate", lectureDao.getRankByDeadDateLectures());
        lectures.put("getRankByLike", lectureDao.getRankByLikeLectures());
        return lectures;
    }

    @Override
    public int lectureFileUpload(LectureFile lectureFile) {
        return lectureDao.lectureFileUpload(lectureFile);
    }

    @Override
    public void setRefundStatus(LectureId lectureId) {
        lectureDao.setRefundStatus(lectureId);
    }

    @Override
    public Long registerLectures(Lecture lecture) {
        return lectureDao.registerLectures(lecture);

    }

    @Override
    public void updateLectures(Lecture lecture) {
        lectureDao.updateLectures(lecture);
    }

    @Override
    public void deleteLectures(Long lectureId, Long memberId) {
        lectureDao.deleteLectures(lectureId, memberId);
    }

    @Override
    public Long getMemberIdById(String memberId) {
        return lectureDao.getMemberIdById(memberId);
    }

    @Override
    public Lecture getLectureDetail(Long lectureId) {
        return lectureDao.getLectureDetail(lectureId);
    }

    @Override
    public boolean checkLikeLectures(Long memberId, Long lectureId) {
        return lectureDao.checkLikeLectures(memberId, lectureId);
    }

    @Override
    public void insertLikeLectures(Long memberId, Long lectureId) {
        lectureDao.insertLikeLectures(memberId, lectureId);
    }

    @Override
    public void deleteLikeLectures(Long memberId, Long lectureId) {
        lectureDao.deleteLikeLectures(memberId, lectureId);
    }

    @Override
    public void updateLikeLectures(Long memberId, Long lectureId, boolean exist) {
        lectureDao.updateLikeLectures(memberId, lectureId, exist);
    }

    @Override
    public Long getLectureMaxId() {
        return lectureDao.getLectureMaxId();
    }

    @Override
    public List<LectureFile> getLectureFiles(Long lectureId) {
        return lectureDao.getLectureFiles(lectureId);
    }

    @Override
    public void insertLectureTags(Map<String, Object> params) {
        lectureDao.insertLectureTags(params);
    }

    @Override
    public void deleteLectureTags(Map<String, Object> params) {
        lectureDao.deleteLectureTags(params);
    }

    @Override
    public List<Long> getExistingTags(Long lectureId) {
        return lectureDao.getExistingTags(lectureId);
    }

    @Override
    public void updateLectureTags(Long lectureId, String tags) {
        List<Long> existTags = lectureDao.getExistingTags(lectureId);

        List<Long> insertedTags = Arrays.stream(tags.replace(" ", "").split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // 지워야 할 태그 목록록
        List<Long> tagsToDelete = existTags.stream()
                .filter(tag -> !insertedTags.contains(tag))
                .collect(Collectors.toList());

        // 입력해야 할 태그 목록
        List<Long> tagsToInsert = insertedTags.stream()
                .filter(tag -> !existTags.contains(tag))
                .collect(Collectors.toList());

        if (!tagsToDelete.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("lectureId", lectureId);
            params.put("tagIds", tagsToDelete);
            lectureDao.deleteLectureTags(params);
        }

        if (!tagsToInsert.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("lectureId", lectureId);
            params.put("tagIds", tagsToInsert);
            lectureDao.insertLectureTags(params);
        }
    }

    @Transactional
    public ResponseEntity<ResponseDto> buyLecture(Map<String, Long> params) {
        // 중복 구매 확인
        if (checkBeforeBuyLecture(params)) {
            return ResponseDto.alreadyBuyed();
        }

        // 잔여 좌석 조회 (for update)
        Integer seats = lectureDao.getSeatsForUpdate(params.get("lectureId"));

        if (seats == null || seats <= 0) {
            throw new RuntimeException("좌석이 모두 매진되었습니다.");
        }

        // 좌석 차감 및 결제 처리
        lectureDao.buyLecture(params);
        lectureDao.insertPayLog(params);

        return ResponseEntity.ok(new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS));
    }

    @Override
    public Boolean checkBeforeBuyLecture(Map<String, Long> params) {
        return lectureDao.checkBeforeBuyLecture(params);
    }

    @Override
    public Boolean checkCanRefund(Map<String, Long> params) {
        return lectureDao.checkCanRefund(params);
    }

    @Override
    public void payRefund(Map<String, Long> params) {
        lectureDao.payRefund(params);
    }

    @Override
    public void insertLectureListByExcel(LectureList lectureList) {
        lectureDao.insertLectureListByExcel(lectureList);
    }

    @Override
    public List<Tag> getTags() {
        return lectureDao.getTags();
    }

    @Override
    public List<LectureList> getLectureLists(Long lectureId) {
        return lectureDao.getLectureLists(lectureId);
    }

    @Override
    public List<LectureList> getLectureListByExcel(MultipartFile excelFile, Long lectureId, Long memberId) {
        List<LectureList> lectureLists = new ArrayList<LectureList>();
        try (XSSFWorkbook excel = new XSSFWorkbook(excelFile.getInputStream())) {
            XSSFSheet workSheet = excel.getSheetAt(0);

            for (int i = 2; i < workSheet.getPhysicalNumberOfRows(); i++) {
                LectureList list = new LectureList();

                DataFormatter formatter = new DataFormatter();
                XSSFRow row = workSheet.getRow(i);

                String title = formatter.formatCellValue(row.getCell(0));
                String description = formatter.formatCellValue(row.getCell(1));
                String date = formatter.formatCellValue(row.getCell(2));
                String start_time = formatter.formatCellValue(row.getCell(3));
                String end_time = formatter.formatCellValue(row.getCell(4));

                list.setLectureId(lectureId);
                list.setMemberId(memberId);

                list.setTitle(title);
                list.setDescription(description);
                list.setDate(date);
                list.setStartTime(start_time);
                list.setEndTime(end_time);

                if (!list.getTitle().isEmpty() && !list.getDescription().isEmpty() && !list.getDate().isEmpty()
                        && !list.getStartTime().isEmpty() && !list.getEndTime().isEmpty()) {
                    lectureLists.add(list);
                }
            }

            return lectureLists;
        } catch (IOException e) {
            e.printStackTrace();
            return lectureLists;
        }
    }

    @Override
    public int isAttend(Map<String, Long> params) {
        return lectureDao.isAttend(params);
    }

}
