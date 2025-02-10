package com.example.myapp.account.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter	
public class EndLecture {
	private String lecture_title;
	private String start_date;
	private String end_date;
	private double course_percent;
	private Long lecture_id;
}
