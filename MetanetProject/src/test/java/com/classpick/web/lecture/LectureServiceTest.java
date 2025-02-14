package com.classpick.web.lecture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.classpick.web.lecture.dao.ILectureRepository;
import com.classpick.web.lecture.model.*;
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
    void getAllLectures_ShouldReturnLectureMap() {
        when(lectureDao.getAllLectures()).thenReturn(Arrays.asList(new Lecture(), new Lecture()));
        when(lectureDao.getRankByDeadDateLectures()).thenReturn(Arrays.asList(new Lecture()));
        when(lectureDao.getRankByLikeLectures()).thenReturn(Arrays.asList(new Lecture()));

        Map<String, List<Lecture>> result = lectureService.getAllLectures();

        assertEquals(3, result.size());
    }

    @Test
    void registerLectures_ShouldReturnLectureId() {
        when(lectureDao.registerLectures(any(Lecture.class))).thenReturn(1L);

        Long result = lectureService.registerLectures(new Lecture());

        assertEquals(1L, result);
    }

    @Test
    void updateLectures_ShouldUpdateLecture() {
        Lecture lecture = new Lecture();
        lectureService.updateLectures(lecture);
        verify(lectureDao, times(1)).updateLectures(lecture);
    }

    @Test
    void deleteLectures_ShouldDeleteLecture() {
        Long lectureId = 1L, memberId = 1L;
        lectureService.deleteLectures(lectureId, memberId);
        verify(lectureDao, times(1)).deleteLectures(lectureId, memberId);
    }

    @Test
    void getLectureDetail_ShouldReturnLecture() {
        when(lectureDao.getLectureDetail(1L)).thenReturn(new Lecture());

        Lecture result = lectureService.getLectureDetail(1L);

        assertNotNull(result);
    }

    @Test
    void checkLikeLectures_ShouldReturnBoolean() {
        when(lectureDao.checkLikeLectures(1L, 1L)).thenReturn(true);

        boolean result = lectureService.checkLikeLectures(1L, 1L);

        assertTrue(result);
    }

    @Test
    void insertLikeLectures_ShouldInsertLike() {
        lectureService.insertLikeLectures(1L, 1L);
        verify(lectureDao, times(1)).insertLikeLectures(1L, 1L);
    }

    @Test
    void deleteLikeLectures_ShouldDeleteLike() {
        lectureService.deleteLikeLectures(1L, 1L);
        verify(lectureDao, times(1)).deleteLikeLectures(1L, 1L);
    }

    @Test
    void updateLikeLectures_ShouldUpdateLikeStatus() {
        lectureService.updateLikeLectures(1L, 1L, true);
        verify(lectureDao, times(1)).updateLikeLectures(1L, 1L, true);
    }

    @Test
    void getLectureMaxId_ShouldReturnMaxId() {
        when(lectureDao.getLectureMaxId()).thenReturn(10L);

        Long result = lectureService.getLectureMaxId();

        assertEquals(10L, result);
    }

    @Test
    void getLectureFiles_ShouldReturnFileList() {
        when(lectureDao.getLectureFiles(1L)).thenReturn(Arrays.asList(new LectureFile()));

        List<LectureFile> result = lectureService.getLectureFiles(1L);

        assertFalse(result.isEmpty());
    }

    @Test
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
    void buyLecture_ShouldInsertPurchaseAndLog() {
        Map<String, Long> params = new HashMap<>();

        lectureService.buyLecture(params);

        verify(lectureDao, times(1)).buyLecture(params);
        verify(lectureDao, times(1)).insertPayLog(params);
    }

    @Test
    void checkBeforeBuyLecture_ShouldReturnBoolean() {
        Map<String, Long> params = new HashMap<>();
        when(lectureDao.checkBeforeBuyLecture(params)).thenReturn(true);

        boolean result = lectureService.checkBeforeBuyLecture(params);

        assertTrue(result);
    }

    @Test
    void checkCanRefund_ShouldReturnBoolean() {
        Map<String, Long> params = new HashMap<>();
        when(lectureDao.checkCanRefund(params)).thenReturn(true);

        boolean result = lectureService.checkCanRefund(params);

        assertTrue(result);
    }

    @Test
    void payRefund_ShouldProcessRefund() {
        Map<String, Long> params = new HashMap<>();

        lectureService.payRefund(params);

        verify(lectureDao, times(1)).payRefund(params);
    }

    @Test
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
}
