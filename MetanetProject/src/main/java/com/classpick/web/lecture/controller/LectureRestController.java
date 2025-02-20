package com.classpick.web.lecture.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.common.response.ResponseMessage;
import com.classpick.web.lecture.model.Lecture;
import com.classpick.web.lecture.model.LectureFile;
import com.classpick.web.lecture.model.LectureId;
import com.classpick.web.lecture.model.LectureList;
import com.classpick.web.lecture.model.Tag;
import com.classpick.web.lecture.service.ILectureService;
import com.classpick.web.util.GetAuthenUser;
import com.classpick.web.util.RegexUtil;
import com.classpick.web.util.S3FileUploader;
import com.classpick.web.zoom.model.ZoomDate;
import com.classpick.web.zoom.model.ZoomMeetingRequest;
import com.classpick.web.zoom.service.ZoomService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api/lectures")
public class LectureRestController {

    @Autowired
    ILectureService lectureService;

    @Autowired
    S3FileUploader s3FileUploader;

    @Autowired
    ZoomService zoomService;

    // 전체 조회 -- 고범준
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<ResponseDto> getAllLectures() {

        Map<String, List<Lecture>> lecture = new HashMap<String, List<Lecture>>();
        try {
            lecture = lectureService.getAllLectures();
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, lecture);
        return ResponseEntity.ok(responseBody);
    }

    // 태그들 조회 -- 고범준
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @GetMapping(value = "/get-tags" , produces = "application/json")
    public ResponseEntity<ResponseDto> getTags() {
        List<Tag> tags = new ArrayList<Tag>();

        try {
            tags = lectureService.getTags();
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, tags);
        return ResponseEntity.ok(responseBody);
    }

    // 특정 강의 조회 -- 고범준
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @GetMapping(value = "/{lecture_id}", produces = "application/json")
    public ResponseEntity<ResponseDto> getLectureDetail(@PathVariable("lecture_id") Long lectureId) {

        Lecture lecture = new Lecture();
        try {
            lecture = lectureService.getLectureDetail(lectureId);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, lecture);
        return ResponseEntity.ok(responseBody);
    }

    // 특정 강의 강의 일정 조회 -- 고범준
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @GetMapping(value= "/lectureLists/{lecture_id}", produces = "application/json")
    public ResponseEntity<ResponseDto> getLectureLists(@PathVariable("lecture_id") Long lectureId) {
        List<LectureList> lectureLists = new ArrayList<LectureList>();
        try {
            lectureLists = lectureService.getLectureLists(lectureId);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, lectureLists);
        return ResponseEntity.ok(responseBody);
    }

    // 좋아요 누른 강의 목록 보기 -- 고범준
    @SuppressWarnings({ "rawtypes" })
    @PostMapping("/likes/{lecture_id}")
    public ResponseEntity<ResponseDto> likeLectures(@PathVariable("lecture_id") Long lectureId) {

        String member_id = GetAuthenUser.getAuthenUser();
        // 인증되지 않은 경우는 바로 처리
        if (member_id == null) {
            return ResponseDto.noAuthentication();
        }

        Long memberId = lectureService.getMemberIdById(member_id);

        boolean exist = lectureService.checkLikeLectures(memberId, lectureId);
        try {
            lectureService.updateLikeLectures(memberId, lectureId, exist);
            if (exist) {
                lectureService.deleteLikeLectures(memberId, lectureId);
            } else {
                lectureService.insertLikeLectures(memberId, lectureId);
            }
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

    // 강의 자료 업로드 (List) form-Data -- 고범준
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @PostMapping("/upload/{lecture_id}")
    public ResponseEntity<ResponseDto> lectureFileUpload(
            @PathVariable("lecture_id") Long lectureId,
            @RequestParam("files") List<MultipartFile> files) {

        String memberId = GetAuthenUser.getAuthenUser();
        // 인증되지 않은 경우는 바로 처리
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }

        Long member_id = lectureService.getMemberIdById(memberId);

        try {
            List<String> urls = s3FileUploader.uploadFiles(files, "lectures", "classFile", member_id);
            for (String url : urls) {
                LectureFile lectureFile = new LectureFile();
                lectureFile.setLectureId(lectureId);
                lectureFile.setMemberId(member_id);
                lectureFile.setFileUrl(url);
                lectureService.lectureFileUpload(lectureFile);
            }
            ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, urls);
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
    }

    // 강의 자료 리스트 -- 고범준
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @GetMapping(value= "/data/{lecture_id}", produces = "application/json")
    public ResponseEntity<ResponseDto> getLectureFiles(
            @PathVariable("lecture_id") Long lectureId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String memberId = "";

        if (authentication != null) {
            // 현재 인증된 사용자 정보
            memberId = authentication.getName();
            log.info(memberId);
        }
        if (memberId == null)
            return ResponseDto.noAuthentication();

        Long member_id = lectureService.getMemberIdById(memberId);

        Map<String, Long> params = new HashMap<String, Long>();
        params.put("memberId", member_id);
        params.put("lectureId", lectureId);

        int is_buyed = lectureService.isAttend(params);

        try {
            List<LectureFile> lectureFiles = lectureService.getLectureFiles(lectureId);
            List<String> urls = new ArrayList<String>();
            urls.add(Integer.toString(is_buyed));
            for (LectureFile lectureFile : lectureFiles) {
                urls.add(lectureFile.getFileUrl());
            }

            ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, urls);
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
    }

    // 환불 불가로 변경 기능 Json -- 고범준
    @SuppressWarnings({ "rawtypes" })
    @PutMapping("/{lecture_id}/refund-status")
    public ResponseEntity<ResponseDto> setRefundStatus(@PathVariable("lecture_id") Long lectureId) {
        String memberId = GetAuthenUser.getAuthenUser();
        // 인증되지 않은 경우는 바로 처리
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }
        Long member_id = lectureService.getMemberIdById(memberId);

        LectureId ids = new LectureId(member_id, lectureId);
        try {
            lectureService.setRefundStatus(ids);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

    // 강의 추가 form-Data -- 고범준
    @SuppressWarnings({ "rawtypes" })
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> lecturelikeLectures(
            @ModelAttribute Lecture lecture,
            @RequestParam(value = "profileFile", required = false) MultipartFile profileFile,
            @RequestPart(value = "lists", required = false) List<LectureList> lists,
            @RequestParam(value = "excelFile", required = false) MultipartFile excelFile,
            @RequestParam(value = "descriptionPicFile", required = false) MultipartFile descriptionPicFile) {

        String memberId = GetAuthenUser.getAuthenUser();
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }
        Long member_id = lectureService.getMemberIdById(memberId);

        try {
            // lecture 값 검증
            lecture.setMemberId(member_id);
            if (lecture.getTitle() == null || lecture.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ResponseDto("REGEX_ERROR", "Title value is required"));
            }
            if (lecture.getDescription() == null || lecture.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("REGEX_ERROR", "Description value is required"));
            }
            if (lecture.getCategory() == null || lecture.getCategory().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ResponseDto("REGEX_ERROR", "Category value is required"));
            }
            if (lecture.getDeadlineTime() == null) {
                return ResponseEntity.badRequest().body(new ResponseDto("REGEX_ERROR", "Deadline time is required"));
            }
            if (lecture.getStartDate() == null) {
                return ResponseEntity.badRequest().body(new ResponseDto("REGEX_ERROR", "Start date is required"));
            }
            if (lecture.getEndDate() == null) {
                return ResponseEntity.badRequest().body(new ResponseDto("REGEX_ERROR", "End date is required"));
            }
            lecture.setLink("null");

            // 강의 생성
            lectureService.registerLectures(lecture);
            Long lectureId = lecture.getLectureId();
            // 강의 생성 후 태그 생성
            if (lecture.getTags() != null && !lecture.getTags().isEmpty()) {
                lectureService.updateLectureTags(lectureId, lecture.getTags());
            }

            // 프로필 파일 업로드
            if (profileFile != null && !profileFile.isEmpty()) {
                String url;
                try {
                    url = s3FileUploader.uploadFile(profileFile, "lectures", "profile", lectureId);
                    lecture.setProfileUrl(url);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseDto.serverError();
                }
            }
            // 설명 파일 업로드
            if (descriptionPicFile != null && !descriptionPicFile.isEmpty()) {
                String url;
                try {
                    url = s3FileUploader.uploadFile(descriptionPicFile, "lectures", "description", lectureId);
                    lecture.setDescriptionPicUrl(url);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseDto.serverError();
                }
            }

            lectureService.updateLectures(lecture);

            zoomService.requestZoomAccessToken(lecture.getCode(), memberId);

            List<LectureList> lectureLists = new ArrayList<>();
            if (excelFile != null && !excelFile.isEmpty()) {

                lectureLists = lectureService.getLectureListByExcel(excelFile, lectureId, member_id);
            } else if (lists != null && !lists.isEmpty()) {

                lectureLists = lists;
            } else {

                return ResponseEntity.badRequest().body(new ResponseDto("DATA_ERROR", "No lecture schedule provided"));
            }

            List<ZoomDate> zoomdates = new ArrayList<>();
            ZoomMeetingRequest zoomRequest = new ZoomMeetingRequest();

            for (LectureList lectureList : lectureLists) {
                LectureList lectureData = new LectureList();
                lectureData.setLectureId(lectureId);
                lectureData.setMemberId(member_id);
                lectureData.setTitle(lectureList.getTitle());
                lectureData.setDescription(lectureList.getDescription());
                lectureData.setStartTime(lectureList.getDate() + "T" + lectureList.getStartTime() + ":00");
                lectureData.setEndTime(lectureList.getDate() + "T" + lectureList.getEndTime() + ":00");

                lectureService.insertLectureListByExcel(lectureData);

                ZoomDate zoomDate = new ZoomDate();
                zoomDate.setLectureListId(lectureData.getLectureListId());
                zoomDate.setDate(LocalDate.parse(lectureList.getDate()));
                zoomDate.setStartTime(LocalTime.parse(lectureList.getStartTime()));
                zoomDate.setEndTime(LocalTime.parse(lectureList.getEndTime()));
                zoomdates.add(zoomDate);
            }

            zoomRequest.setZoomDates(zoomdates);
            zoomService.createMeeting(member_id, zoomRequest);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

    // 강의 내용 수정 form-Data -- 고범준
    @SuppressWarnings({ "rawtypes" })
    @PutMapping(value = "/update-form/{lecture_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto> updateLectures(
            @ModelAttribute Lecture lecture,
            @PathVariable("lecture_id") Long lectureId,
            @RequestParam(value = "profileFile", required = false) MultipartFile profileFile,
            @RequestParam(value = "descriptionPicFile", required = false) MultipartFile descriptionPicFile) {

        String memberId = GetAuthenUser.getAuthenUser();
        // 인증되지 않은 경우는 바로 처리
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }

        Long member_id = lectureService.getMemberIdById(memberId);
        lecture.setLectureId(lectureId);
        try {
            RegexUtil regexUtil = new RegexUtil();
            lecture.setMemberId(member_id);

            if (lecture.getDeadlineTime() != null && !regexUtil.checkDate(lecture.getDeadlineTime())) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("REGEX_ERROR", "Deadline time is required"));
            }
            if (lecture.getStartDate() != null && !regexUtil.checkDate(lecture.getStartDate())) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("REGEX_ERROR", "Start date is required"));
            }
            if (lecture.getEndDate() != null && !regexUtil.checkDate(lecture.getEndDate())) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("REGEX_ERROR", "End date is required"));
            }
            if (profileFile != null && !profileFile.isEmpty()) {
                String url;
                try {
                    url = s3FileUploader.uploadFile(profileFile, "lectures", "profile", lectureId);
                } catch (Exception e) {
                    return ResponseDto.serverError();
                }
                lecture.setProfileUrl(url);
            }
            if (descriptionPicFile != null && !descriptionPicFile.isEmpty()) {
                String url;
                try {
                    url = s3FileUploader.uploadFile(descriptionPicFile, "lectures", "description", lectureId);
                } catch (Exception e) {
                    return ResponseDto.serverError();
                }
                lecture.setDescriptionPicUrl(url);
            }

            // tags 를 제외한 다른 값들 중, 수정사항이 있는지 확인
            if (lecture.getTitle() != null || lecture.getProfileUrl() != null ||
                    lecture.getDescription() != null || lecture.getDescriptionPicUrl() != null ||
                    lecture.getCategory() != null || lecture.getPrice() != null ||
                    lecture.getLimitStudent() != null || lecture.getDeadlineTime() != null ||
                    lecture.getLecturesDate() != null || lecture.getStartDate() != null ||
                    lecture.getEndDate() != null || lecture.getDeleted() != null) {

                // 하나라도 값이 있으면 업데이트 실행
                lectureService.updateLectures(lecture);
            }

            if (lecture.getTags() != null && !lecture.getTags().isEmpty()) {
                lectureService.updateLectureTags(lectureId, lecture.getTags());
            }

        } catch (Exception e) {
            return ResponseDto.databaseError();
        }

        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

    // 강의 내용 수정 JSON -- 고범준
    @SuppressWarnings({ "rawtypes" })
    @PutMapping(value = "/update-json/{lecture_id}")
    public ResponseEntity<ResponseDto> updateLectures(
            @RequestBody Lecture lecture,
            @PathVariable("lecture_id") Long lectureId) {

        String memberId = GetAuthenUser.getAuthenUser();
        // 인증되지 않은 경우는 바로 처리
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }

        Long member_id = lectureService.getMemberIdById(memberId);
        lecture.setLectureId(lectureId);
        try {
            RegexUtil regexUtil = new RegexUtil();
            lecture.setMemberId(member_id);

            if (lecture.getDeadlineTime() != null && !regexUtil.checkDate(lecture.getDeadlineTime())) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("REGEX_ERROR", "Deadline time is required"));
            }
            if (lecture.getStartDate() != null && !regexUtil.checkDate(lecture.getStartDate())) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("REGEX_ERROR", "Start date is required"));
            }
            if (lecture.getEndDate() != null && !regexUtil.checkDate(lecture.getEndDate())) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDto("REGEX_ERROR", "End date is required"));
            }

            // tags 를 제외한 다른 값들 중, 수정사항이 있는지 확인
            if (lecture.getTitle() != null || lecture.getProfileUrl() != null ||
                    lecture.getDescription() != null || lecture.getDescriptionPicUrl() != null ||
                    lecture.getCategory() != null || lecture.getPrice() != null ||
                    lecture.getLimitStudent() != null || lecture.getDeadlineTime() != null ||
                    lecture.getLecturesDate() != null || lecture.getStartDate() != null ||
                    lecture.getEndDate() != null || lecture.getDeleted() != null) {

                // 하나라도 값이 있으면 업데이트 실행
                lectureService.updateLectures(lecture);
            }

            if (lecture.getTags() != null && !lecture.getTags().isEmpty()) {
                lectureService.updateLectureTags(lectureId, lecture.getTags());
            }

        } catch (Exception e) {
            return ResponseDto.databaseError();
        }

        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

    // 강의 삭제 Json -- 고범준
    @SuppressWarnings({ "rawtypes" })
    @DeleteMapping("/delete/{lecture_id}")
    public ResponseEntity<ResponseDto> deleteLectures(@PathVariable("lecture_id") Long lectureId) {
        String memberId = GetAuthenUser.getAuthenUser();
        // 인증되지 않은 경우는 바로 처리
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }

        Long member_id = lectureService.getMemberIdById(memberId);
        try {
            lectureService.deleteLectures(lectureId, member_id);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

    // 강의 구매 - 고범준 (한채은 수정)
    @PostMapping("/buy/{lecture_id}")
    public ResponseEntity<ResponseDto> buyLecture(@PathVariable("lecture_id") Long lectureId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return ResponseDto.noAuthentication();
        }

        String memberId = authentication.getName();
        Long memberUID = lectureService.getMemberIdById(memberId);

        Map<String, Long> params = new HashMap<>();
        params.put("memberId", memberUID);
        params.put("lectureId", lectureId);

        try {
            return lectureService.buyLecture(params);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
    }

    // 환불하기 -- 고범준
    @SuppressWarnings({ "rawtypes" })
    @PostMapping("/refund/{lecture_id}")
    public ResponseEntity<ResponseDto> lectureRefund(@PathVariable("lecture_id") Long lectureId) {
        String memberId = GetAuthenUser.getAuthenUser();
        // 인증되지 않은 경우는 바로 처리
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }

        Long member_id = lectureService.getMemberIdById(memberId);

        Map<String, Long> params = new HashMap<String, Long>();
        params.put("memberId", member_id);
        params.put("lectureId", lectureId);
        try {
            if (!lectureService.checkCanRefund(params)) {
                return ResponseDto.cantRefund();
            }
            lectureService.payRefund(params);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

}
