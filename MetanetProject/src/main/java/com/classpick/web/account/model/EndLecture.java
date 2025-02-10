package com.classpick.web.account.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter	
public class EndLecture {
	private String title;
	private String startDate;
	private String endDate;
	private double coursePercent;
	private Long lectureId;
}
