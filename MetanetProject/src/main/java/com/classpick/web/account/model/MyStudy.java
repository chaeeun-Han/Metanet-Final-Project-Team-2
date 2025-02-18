package com.classpick.web.account.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class MyStudy {
	private Long lectureId;
	private String title;
	private String startTime;
	private String endTime;
	private double attendPercent;
	private List<MyStudyLectureList> myStudyLectureList;
	
}
