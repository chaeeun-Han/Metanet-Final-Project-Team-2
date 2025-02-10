package com.example.myapp.account.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyStudyLectureList {
	private Long lecture_list_id;
	private String title;
	private boolean is_attend;
	private String attend_status;
	private String start_time;
	private String end_time;
	private Long lecture_id;
}
