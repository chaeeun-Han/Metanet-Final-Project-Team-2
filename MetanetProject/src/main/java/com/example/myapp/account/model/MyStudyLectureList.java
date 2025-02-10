package com.example.myapp.account.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyStudyLectureList {
	private Long lectureListId;
	private String title;
	private boolean isAttend;	
	private String startTime;
	private String endTime;
	private Long lectureId;
}
