package com.classpick.web.account.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngLecture {
	private String lectureListId;
	private String endTime;
	private String startTime;
	private double attendPercent;
	private Long lectureId;
	private String title;
}
