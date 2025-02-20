package com.classpick.web.lecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.lecture.model.Lecture;
import com.classpick.web.lecture.model.LectureFile;
import com.classpick.web.lecture.model.LectureList;
import com.classpick.web.lecture.service.LectureService;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

    @Mock
    private ILectureRepository lectureDao;

    @Mock
    private MultipartFile excelFile;

    @InjectMocks
    private LectureService lectureService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("모든 강의 조회 - 성공")
    void getAllLectures_ShouldReturnLectureMap() {
        when(lectureDao.getAllLectures()).thenReturn(Arrays.asList(new Lecture(), new Lecture()));
        when(lectureDao.getRankByDeadDateLectures()).thenReturn(Arrays.asList(new Lecture()));
        when(lectureDao.getRankByLikeLectures()).thenReturn(Arrays.asList(new Lecture()));

        Map<String, List<Lecture>> result = lectureService.getAllLectures();

        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("강의 등록 - 성공")
    void registerLectures_ShouldReturnLectureId() {
        when(lectureDao.registerLectures(any(Lecture.class))).thenReturn(1L);

        Long result = lectureService.registerLectures(new Lecture());

        assertEquals(1L, result);
    }

    @Test
    @DisplayName("강의 수정 - 성공")
    void updateLectures_ShouldUpdateLecture() {
        Lecture lecture = new Lecture();
        lectureService.updateLectures(lecture);
        verify(lectureDao, times(1)).updateLectures(lecture);
    }

    @Test
    @DisplayName("강의 삭제 - 성공")
    void deleteLectures_ShouldDeleteLecture() {
        Long lectureId = 1L, memberId = 1L;
        lectureService.deleteLectures(lectureId, memberId);
        verify(lectureDao, times(1)).deleteLectures(lectureId, memberId);
    }

    @Test
    @DisplayName("강의 상세 조회 - 성공")
    void getLectureDetail_ShouldReturnLecture() {
        when(lectureDao.getLectureDetail(1L)).thenReturn(new Lecture());

        Lecture result = lectureService.getLectureDetail(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("강의 좋아요 - 성공")
    void checkLikeLectures_ShouldReturnBoolean() {
        when(lectureDao.checkLikeLectures(1L, 1L)).thenReturn(true);

        boolean result = lectureService.checkLikeLectures(1L, 1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("좋아요 한 강의 등록 - 성공")
    void insertLikeLectures_ShouldInsertLike() {
        lectureService.insertLikeLectures(1L, 1L);
        verify(lectureDao, times(1)).insertLikeLectures(1L, 1L);
    }

    @Test
    @DisplayName("좋아요 한 강의 삭제 - 성공")
    void deleteLikeLectures_ShouldDeleteLike() {
        lectureService.deleteLikeLectures(1L, 1L);
        verify(lectureDao, times(1)).deleteLikeLectures(1L, 1L);
    }

    @Test
    @DisplayName("좋아요 한 강의 수정 - 성공")
    void updateLikeLectures_ShouldUpdateLikeStatus() {
        lectureService.updateLikeLectures(1L, 1L, true);
        verify(lectureDao, times(1)).updateLikeLectures(1L, 1L, true);
    }

    @Test
    @DisplayName("가장 큰 강의 아이디 조회 - 성공")
    void getLectureMaxId_ShouldReturnMaxId() {
        when(lectureDao.getLectureMaxId()).thenReturn(10L);

        Long result = lectureService.getLectureMaxId();

        assertEquals(10L, result);
    }

    @Test
    @DisplayName("강의 자료 조회 - 성공")
    void getLectureFiles_ShouldReturnFileList() {
        when(lectureDao.getLectureFiles(1L)).thenReturn(Arrays.asList(new LectureFile()));

        List<LectureFile> result = lectureService.getLectureFiles(1L);

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("강의 태그 수정 - 성공")
    void updateLectureTags_ShouldInsertAndDeleteTags() {
        Long lectureId = 1L;
        String tags = "1,2,3";
        List<Long> existingTags = Arrays.asList(2L, 3L, 4L);

        when(lectureDao.getExistingTags(lectureId)).thenReturn(existingTags);

        lectureService.updateLectureTags(lectureId, tags);

        verify(lectureDao, times(1)).deleteLectureTags(any());
        verify(lectureDao, times(1)).insertLectureTags(any());
    }

    @Test
    @DisplayName("강의 구매 - 성공")
    void buyLecture_ShouldInsertPurchaseAndLog() {
        Map<String, Long> params = new HashMap<>();

        lectureService.buyLecture(params);

        verify(lectureDao, times(1)).buyLecture(params);
        verify(lectureDao, times(1)).insertPayLog(params);
    }

    @Test
    @DisplayName("강의 샀는지 여부 체크 - 성공")
    void checkBeforeBuyLecture_ShouldReturnBoolean() {
        Map<String, Long> params = new HashMap<>();
        when(lectureDao.checkBeforeBuyLecture(params)).thenReturn(true);

        boolean result = lectureService.checkBeforeBuyLecture(params);

        assertTrue(result);
    }

    @Test
    @DisplayName("강의 환불 여부 체크 - 성공")
    void checkCanRefund_ShouldReturnBoolean() {
        Map<String, Long> params = new HashMap<>();
        when(lectureDao.checkCanRefund(params)).thenReturn(true);

        boolean result = lectureService.checkCanRefund(params);

        assertTrue(result);
    }

    @Test
    @DisplayName("환불 - 성공")
    void payRefund_ShouldProcessRefund() {
        Map<String, Long> params = new HashMap<>();

        lectureService.payRefund(params);

        verify(lectureDao, times(1)).payRefund(params);
    }

    @Test
    @DisplayName("엑셀 파일에서 강의 일정 파싱 - 성공")
    void getLectureListByExcel_ShouldReturnParsedLectureList() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Sheet1");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        byte[] excelBytes = outputStream.toByteArray();
        MultipartFile excelFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        List<LectureList> result = lectureService.getLectureListByExcel(excelFile, 1L, 1L);

        assertNotNull(result);
    }
    
    @Test
    @DisplayName("빈 액셀 파일 업로드 - 성공")
    void getLectureListByExcel_ShouldReturnEmptyList_WhenExcelFileIsEmpty() throws IOException {
        // 빈 엑셀 파일 생성
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Sheet1");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        byte[] excelBytes = outputStream.toByteArray();
        MultipartFile emptyExcelFile = new MockMultipartFile("file", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        List<LectureList> result = lectureService.getLectureListByExcel(emptyExcelFile, 1L, 1L);

        assertTrue(result.isEmpty(), "빈 엑셀 파일이지만 리스트가 비어있지 않음");
    }

    @Test
    @DisplayName("엑셀 파일에서 강의 일정 파싱(빈 행 무시) - 성공")
    void getLectureListByExcel_ShouldIgnoreRows_WhenMandatoryFieldsAreEmpty() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        sheet.createRow(0);
        sheet.createRow(1);

        var row1 = sheet.createRow(2);
        row1.createCell(0).setCellValue("");
        row1.createCell(1).setCellValue("설명");
        row1.createCell(2).setCellValue("2025-02-15");
        row1.createCell(3).setCellValue("09:00");
        row1.createCell(4).setCellValue("10:00");

        var row2 = sheet.createRow(3);
        row2.createCell(0).setCellValue("강의 제목");
        row2.createCell(1).setCellValue("설명");
        row2.createCell(2).setCellValue("2025-02-15");
        row2.createCell(3).setCellValue("09:00");
        row2.createCell(4).setCellValue("10:00");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        MultipartFile excelFile = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        List<LectureList> result = lectureService.getLectureListByExcel(excelFile, 1L, 1L);

        assertEquals(1, result.size(), "필수 값이 없는 행이 포함됨");
        assertEquals("강의 제목", result.get(0).getTitle(), "올바른 데이터가 포함되지 않음");
    }
    
    @Test
    @DisplayName("엑셀 강의 상세 일정 조회 - 성공")
    void getLectureListByExcel_ShouldParseValidExcelData() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        sheet.createRow(0);
        sheet.createRow(1);

        var row = sheet.createRow(2);
        row.createCell(0).setCellValue("강의 제목");
        row.createCell(1).setCellValue("설명");
        row.createCell(2).setCellValue("2025-02-15");
        row.createCell(3).setCellValue("09:00");
        row.createCell(4).setCellValue("10:00");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelBytes = outputStream.toByteArray();

        MultipartFile excelFile = new MockMultipartFile("file", "valid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        List<LectureList> result = lectureService.getLectureListByExcel(excelFile, 1L, 1L);

        assertFalse(result.isEmpty(), "정상 데이터가 있음에도 리스트가 비어있음");
        assertEquals(1, result.size(), "예상보다 많은/적은 데이터가 포함됨");
        assertEquals("강의 제목", result.get(0).getTitle(), "강의 제목이 올바르게 파싱되지 않음");
        assertEquals("설명", result.get(0).getDescription(), "설명이 올바르게 파싱되지 않음");
        assertEquals("2025-02-15", result.get(0).getDate(), "날짜가 올바르게 파싱되지 않음");
        assertEquals("09:00", result.get(0).getStartTime(), "시작 시간이 올바르게 파싱되지 않음");
        assertEquals("10:00", result.get(0).getEndTime(), "종료 시간이 올바르게 파싱되지 않음");
    }


}
