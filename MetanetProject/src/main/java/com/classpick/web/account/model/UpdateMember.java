package com.classpick.web.account.model;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMember {
	private MultipartFile file;
	private String name;
	private String phone;
	private String birth;
	private String attendId;
	private String tags;
}
