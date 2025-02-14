package com.classpick.web.certification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classpick.web.certification.dao.ICertificationRepository;
import com.classpick.web.certification.dto.Certification;
import com.classpick.web.certification.service.CertificationService;
import com.classpick.web.member.dao.IMemberRepository;
import com.lowagie.text.DocumentException;

@ExtendWith(MockitoExtension.class)
class CertificationServiceTest {

    @Mock
    private ICertificationRepository certificationRepository;

    @Mock
    private IMemberRepository memberRepository;

    @InjectMocks
    private CertificationService certificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCertification_ShouldReturnPdfByteArray_WhenValidInputs() throws Exception {
        String memberName = "Test User";
        Certification certification = new Certification();
        certification.setTitle("Java Course");
        certification.setStart_date("2024-01-01");
        certification.setEnd_date("2024-03-01");

        when(certificationRepository.getNameByUser(anyLong())).thenReturn(memberName);
        when(certificationRepository.getLecutre_title(anyString(), anyString())).thenReturn(certification);

        byte[] mockPdf = new byte[]{1, 2, 3};
        CertificationService spyService = spy(certificationService);
        doReturn(mockPdf).when(spyService).generatePdfFromHtml(anyString());

        byte[] result = spyService.getCertification(1L, "L123");

        assertNotNull(result);
        assertEquals(mockPdf.length, result.length);
    }

    @Test
    void getCertification_ShouldHandleException_WhenPdfGenerationFails() throws Exception {
        String memberName = "Test User";
        Certification certification = new Certification();
        certification.setTitle("Java Course");
        certification.setStart_date("2024-01-01");
        certification.setEnd_date("2024-03-01");

        when(certificationRepository.getNameByUser(anyLong())).thenReturn(memberName);
        when(certificationRepository.getLecutre_title(anyString(), anyString())).thenReturn(certification);

        CertificationService spyService = spy(certificationService);
        doThrow(new DocumentException("PDF 생성 실패")).when(spyService).generatePdfFromHtml(anyString());

        byte[] result = spyService.getCertification(1L, "L123");

        assertNull(result);
    }

    @Test
    void generatePdfFromHtml_ShouldReturnPdfByteArray_WhenValidHtml() throws Exception {

        String validHtml = "<html><body><h1>Test PDF</h1></body></html>";

        byte[] result = certificationService.generatePdfFromHtml(validHtml);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generatePdfFromHtml_ShouldHandleIOException() throws Exception {

        String invalidHtml = "<html><body><h1>Test PDF</h1></body></html>";
        CertificationService spyService = spy(certificationService);
        doThrow(new IOException("파일 접근 오류")).when(spyService).generatePdfFromHtml(invalidHtml);

        assertThrows(IOException.class, () -> spyService.generatePdfFromHtml(invalidHtml));
    }

    @Test
    void generateCertificateHtml_ShouldReturnFormattedHtml_WhenValidInputs() {

        String memberName = "Test User";
        String title = "Java Course";
        String startDate = "2024-01-01";
        String endDate = "2024-03-01";

        String result = certificationService.generateCertificateHtml(memberName, title, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.contains(memberName));
        assertTrue(result.contains(title));
        assertTrue(result.contains(startDate));
        assertTrue(result.contains(endDate));
    }

    @Test
    void checkCourable_ShouldReturnTrue_WhenUserIsEligible() {
        when(certificationRepository.getCourseable(anyLong(), anyString())).thenReturn(true);

        boolean result = certificationService.checkCourable(1L, "L123");

        assertTrue(result);
    }

    @Test
    void checkCourable_ShouldReturnFalse_WhenUserIsNotEligible() {
        when(certificationRepository.getCourseable(anyLong(), anyString())).thenReturn(false);

        boolean result = certificationService.checkCourable(1L, "L123");

        assertFalse(result);
    }
}
