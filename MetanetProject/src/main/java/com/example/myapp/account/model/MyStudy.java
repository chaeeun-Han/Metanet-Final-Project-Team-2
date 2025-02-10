package com.example.myapp.account.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class MyStudy {
	private Long lecture_id;
	private String title;
	private String startTime;
	private String endTime;
	private double attend_percent;
	private List<MyStudyLectureList> myStudyLectureList;
	
}
