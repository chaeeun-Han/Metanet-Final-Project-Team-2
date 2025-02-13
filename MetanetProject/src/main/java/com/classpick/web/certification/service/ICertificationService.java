package com.classpick.web.certification.service;

public interface ICertificationService {

	boolean checkCourable(Long memberUID, Long lecture_id);

	byte[] getCertification(Long memberUID, Long lecture_id);

}
