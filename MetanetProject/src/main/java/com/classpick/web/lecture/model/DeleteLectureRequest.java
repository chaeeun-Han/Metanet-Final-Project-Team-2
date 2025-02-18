package com.classpick.web.lecture.model;

import java.util.List;

import lombok.Data;

@Data
public class DeleteLectureRequest {
	private List<Long> lectureIds;
}
