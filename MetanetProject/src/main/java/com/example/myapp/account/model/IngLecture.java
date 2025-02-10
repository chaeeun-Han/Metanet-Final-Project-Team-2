package com.example.myapp.account.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngLecture {
	private String lecture_list_title;
	private String end_time;
	private String start_time;
	private double attend_percent;
	private Long lecture_id;
}
