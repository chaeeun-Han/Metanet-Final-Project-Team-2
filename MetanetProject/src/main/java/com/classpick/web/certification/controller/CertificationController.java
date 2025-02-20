package com.classpick.web.certification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classpick.web.certification.service.ICertificationService;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.member.dao.IMemberRepository;
import com.classpick.web.util.GetAuthenUser;

@RestController
@RequestMapping("/api/certification")
public class CertificationController {

	@Autowired
	ICertificationService certificationService;

	@Autowired
	IMemberRepository memberRepository;

	@GetMapping
	public ResponseEntity<?> getCertification(@RequestParam("lecture_id") Long lectureId) {
	    String user = GetAuthenUser.getAuthenUser();
	    if (user == null) {
	        return ResponseDto.noAuthentication();
	    }

	    Long memberUID = null;
	    try {
	        memberUID = memberRepository.getMemberIdById(user);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseDto.databaseError();
	    }

	    if (certificationService.checkCourable(memberUID, lectureId)) {
	        try {
	            byte[] pdfContent = certificationService.getCertification(memberUID, lectureId);
	            return ResponseDto.successPdf(pdfContent);
	        } catch (Exception e) {
	            ResponseDto responseBody = new ResponseDto("PDF_ERROR", "Error rendering PDF");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
	        }
	    } else {
	        return ResponseDto.certificateFail();
	    }
	}

}
