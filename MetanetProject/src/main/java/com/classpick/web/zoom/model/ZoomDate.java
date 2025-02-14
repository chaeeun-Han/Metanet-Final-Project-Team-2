package com.classpick.web.zoom.model;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ZoomDate {
	private Long lectureListId;
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
}