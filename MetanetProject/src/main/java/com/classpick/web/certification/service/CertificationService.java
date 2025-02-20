package com.classpick.web.certification.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.classpick.web.certification.dao.ICertificationRepository;
import com.classpick.web.certification.dto.Certification;
import com.classpick.web.member.dao.IMemberRepository;
import com.itextpdf.text.pdf.BaseFont;
import com.lowagie.text.DocumentException;

import ch.qos.logback.core.recovery.ResilientSyslogOutputStream;
@Service
public class CertificationService implements ICertificationService {

	@Autowired
	ICertificationRepository certificationRepository;

	@Autowired
	IMemberRepository memberRepository;

	@Override
	public byte[] getCertification(Long memberUID, Long lecture_id) {

		Certification certification = new Certification();
		byte[] result = null;

		String memberName = certificationRepository.getNameByUser(memberUID);

		certification = certificationRepository.getLecutre_title(memberName, lecture_id);

		String htmlContent = generateCertificateHtml(memberName, certification.getTitle(),
				certification.getStart_date(), certification.getEnd_date());
		try {
			result = generatePdfFromHtml(htmlContent);
		} catch (DocumentException e) {		
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}

		return result;
	}

	public byte[] generatePdfFromHtml(String htmlContent) throws DocumentException, IOException {
        ITextRenderer renderer = new ITextRenderer();

        // 폰트 경로 설정 (MALGUNSL.TTF 또는 다른 폰트)
        String fontPath = new ClassPathResource("/pdf/NanumBarunGothic.ttf")
                    .getURL()
                    .toString();
      
        // 한글 폰트를 설정 (폰트 경로, 인코딩, 내장 여부)
        renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        // 클래스패스에 있는 이미지 경로 불러오기
        String imageResource = new ClassPathResource("/pdf/check.jpg").getURL()
                .toString(); 
       
        // HTML 콘텐츠 내의 이미지 경로를 클래스패스 이미지 경로로 교체
        String modifiedHtmlContent = htmlContent.replace("src=\"image-placeholder.jpg\"", "src=\"" + imageResource + "\"");

        // HTML 문서 설정
        renderer.setDocumentFromString(modifiedHtmlContent);
        renderer.layout();

        // PDF 생성
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream outputStream = byteArrayOutputStream;
        renderer.createPDF(outputStream);
        outputStream.close();

        return byteArrayOutputStream.toByteArray();
    }
	
	public String generateCertificateHtml(String memberName, String title, String start_date, String end_date) {
	    return "<!DOCTYPE html>" +
	            "<html lang=\"ko\">" +
	            "<head>" +
	            "<meta charset=\"UTF-8\">" + "</meta>" +
	            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" + "</meta>" +
	            "<title>수료증</title>" +
	            "<style>" +
	            "@page {" +
	            "  size: A4;" + // A4 용지 크기 설정
	            "  margin: 0;" + // 여백 없애기
	            "}" +
	            "body {" +
	            "  font-family: 'NanumBarunGothic', sans-serif;" +
	            "  margin: 0;" +
	            "  padding: 0;" +
	            "  height: 100%;" +
	            "  display: flex;" +
	            "  justify-content: center;" + // 페이지 중앙 정렬
	            "  align-items: center;" +
	            "  background-color: #f4f4f4;" +
	            "}" +
	            ".certificate-container {" +
	            "  width: 100%;" + // 페이지 가로 크기에 맞춤
	            "  height: 100%;" + // 페이지 세로 크기에 맞춤
	            "  padding: 30px;" + // 패딩을 적당히 조정
	            "  border: 5px solid #5F92FF;" +
	            "  background-color: #ffffff;" +
	            "  text-align: center;" +
	            "  border-radius: 10px;" +
	            "  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);" +
	            "}" +
	            ".certificate-container-img {" +
	            "  width: 15%;" + // 이미지 크기 조정
	            "  opacity: 0.9;" +
	            "  margin-bottom: 20px;" +
	            "  position: absolute;" +
	            "  top: 10%;" +
	            "  right: 10%;" +
	            "  transform: translateX(0);" +
	            "}" +
	            ".left-align {" +
	            "  text-align: left;" +
	            "  margin-bottom: 20px;" +
	            "}" +
	            "h1 {" +
	            "  color: #5F92FF;" +
	            "  font-size: 36px;" +
	            "  margin-bottom: 30px;" +
	            "}" +
	            "p {" +
	            "  font-size: 20px;" +
	            "  line-height: 1.6;" +
	            "  margin: 10px 0;" +
	            "}" +
	            ".strong {" +
	            "  font-weight: bold;" +
	            "}" +
	            ".certificate-footer {" +
	            "  margin-top: 50px;" +
	            "  font-size: 16px;" +
	            "  color: #777;" +
	            "}" +
	            ".signature {" +
	            "  margin-top: 30px;" +
	            "  font-size: 18px;" +
	            "  color: #333;" +
	            "}" +
	            ".lectureTitle {" +
	            "  text-decoration: underline;" +
	            "  font-weight: bold;" +
	            "}" +
	            "</style>" +
	            "</head>" +
	            "<body>" +
	            "<div id=\"certificate\" class=\"certificate-container\">" +
	            "<h1>클래스픽 수료증</h1>" +    "<br>" + "</br>" +"<br>" + "</br>" +            
	            "<div class=\"left-align\">"+
	            "<p><span class=\"strong\">이름:</span> " + memberName + "</p>" +"<br>" + "</br>" + 
	            "<p><span class=\"strong\">강의명:</span> " + title + "</p>" +"<br>" + "</br>" + 
	            "<p><span class=\"strong\">수료일:</span> " + start_date + " ~ " + end_date + "</p>" +"<br>" + "</br>" + 
	            "</div>" +"<br>" + "</br>" +"<br>" + "</br>" +"<br>" + "</br>" + 
	            "<p>위 사람은 <span class=\"lectureTitle\">" + title + "</span> 강의를 성실히 수행하였기에 증서를 드립니다.</p>" +
	            "<br>" + "</br>" + "<br>" + "</br>" +"<br>" + "</br>" +
	            "<p>본 수료증은 공식 인증서로, 무단 복제 및 변경을 금지합니다.</p>" +"<br>" + "</br>" +
	            "<img src=\"image-placeholder.jpg\" alt=\"check image\" class=\"certificate-container-img\"/>" +
	            "<br>" + "</br>" +"<br>" + "</br>" +"<br>" + "</br>" +"<br>" + "</br>" +
	            "<div class=\"certificate-footer\">" +
	            "<p>발급일: <span class=\"strong\">" + end_date + "</span></p>" +
	            "</div>" +
	            "</div>" +
	            "</body>" +
	            "</html>";
	}




	@Override
	public boolean checkCourable(Long memberUID, Long lecture_id) {		
		if (certificationRepository.getCourseable(memberUID, lecture_id)) {
			return true;
		} else {
			return false;
		}
	}

}
