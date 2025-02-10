package com.classpick.web.lecture.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlySalesDto {
	private final String month;
	private final int totalRevenue;
}
