package com.classpick.web.account.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonWriter.Members;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classpick.web.account.dao.IAccountRepository;
import com.classpick.web.account.model.AccountLecture;
import com.classpick.web.account.model.DueToLecture;
import com.classpick.web.account.model.EndLecture;
import com.classpick.web.account.model.IngLecture;
import com.classpick.web.account.model.MyStudy;
import com.classpick.web.account.model.MyStudyLectureList;
import com.classpick.web.account.model.Pay;
import com.classpick.web.account.model.TeacherLecture;
import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.common.response.ResponseMessage;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.util.S3FileUploader;

@Service
public class AccountService implements IAccountService {

    @Autowired
    IAccountRepository accountRepository;

    @Autowired
    IMemberRepository memberRepository;

    @Autowired
    S3FileUploader s3FileUploader;

    @Override
    public ResponseEntity<ResponseDto> getLecture(String memberId) {
        List<AccountLecture> result = null;
        try {
            Long memberUID = memberRepository.getMemberIdById(memberId);
            result = accountRepository.getLecture(memberUID);

            // 데이터가 없을 경우 빈 리스트 반환
            if (result == null || result.isEmpty()) {
                result = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, result);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ResponseDto> insertCategory(String tags, String memberId) {
        Long memberUID = memberRepository.getMemberIdById(memberId);
        String[] categories = tags.split(",");

        try {
            for (String cat : categories) {
                accountRepository.insertCategory(memberUID, cat.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ResponseDto> updateProfile(String user, MultipartFile file) {
        Long memberUID = memberRepository.getMemberIdById(user);
        Long memberid = Long.valueOf(memberUID);

        String fileUrl = s3FileUploader.uploadFile(file, "members", "profile", memberid);

        try {
            accountRepository.updateProfile(memberUID, fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ResponseDto> getMyPage(String user) {
        Long memberUID = memberRepository.getMemberIdById(user);
        List<Members> result = null;

        try {
            result = accountRepository.getMyPage(memberUID);

            // 데이터가 없을 경우 빈 리스트 반환
            if (result == null || result.isEmpty()) {
                result = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, result);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ResponseDto> getPaylog(String user) {
        Long memberUID = memberRepository.getMemberIdById(user);
        List<Pay> result = null;

        try {
            result = accountRepository.getPaylog(memberUID);

            // 데이터가 없을 경우 빈 리스트 반환
            if (result == null || result.isEmpty()) {
                result = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, result);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ResponseDto> getMyStudy(String user) {
        Long memberUID = memberRepository.getMemberIdById(user);

        // MyStudy 기본 정보를 조회
        List<MyStudy> results = accountRepository.getMyStudy(memberUID);

        // 데이터가 없을 경우 빈 리스트 반환
        if (results == null || results.isEmpty()) {
            results = new ArrayList<>();
        }

        // MyStudyLectureList를 조회하여 각 MyStudy 객체에 설정
        for (MyStudy myStudy : results) {
            // 각 MyStudy에 해당하는 강의 목록을 조회
            List<MyStudyLectureList> lectureLists = accountRepository.getMyStudyLectureList(myStudy.getLectureId(),
                    memberUID);

            // 데이터가 없을 경우 빈 리스트 반환
            if (lectureLists == null || lectureLists.isEmpty()) {
                myStudy.setMyStudyLectureList(new ArrayList<>());
            } else {
                myStudy.setMyStudyLectureList(lectureLists);
            }
        }

        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, results);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ResponseDto> getMyLecture(String user) {
        Long memberUID = memberRepository.getMemberIdById(user);

        TeacherLecture results = getTeacherLectures(memberUID);

        // 모든 강의 리스트가 비어 있으면 빈 데이터를 반환
        if (results == null || (results.getDueToLecture().isEmpty() && results.getIngLecture().isEmpty()
                && results.getEndLecture().isEmpty())) {
            results = new TeacherLecture();
            results.setDueToLecture(new ArrayList<>());
            results.setIngLecture(new ArrayList<>());
            results.setEndLecture(new ArrayList<>());
        }

        ResponseDto responseBody = new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, results);
        return ResponseEntity.ok(responseBody);
    }

    public TeacherLecture getTeacherLectures(Long teacherId) {
        // 강의 상태별 데이터 조회
        List<DueToLecture> dueToLectures = accountRepository.getDueToLectures(teacherId);
        List<IngLecture> ingLectures = accountRepository.getIngLectures(teacherId);
        List<EndLecture> endLectures = accountRepository.getEndLectures(teacherId);

        // 데이터가 없을 경우 빈 리스트 반환
        if (dueToLectures == null || dueToLectures.isEmpty()) {
            dueToLectures = new ArrayList<>();
        }
        if (ingLectures == null || ingLectures.isEmpty()) {
            ingLectures = new ArrayList<>();
        }
        if (endLectures == null || endLectures.isEmpty()) {
            endLectures = new ArrayList<>();
        }

        // TeacherLecture 객체 생성 후 데이터 설정
        TeacherLecture teacherLecture = new TeacherLecture();
        teacherLecture.setDueToLecture(dueToLectures);
        teacherLecture.setIngLecture(ingLectures);
        teacherLecture.setEndLecture(endLectures);

        return teacherLecture;
    }
}

