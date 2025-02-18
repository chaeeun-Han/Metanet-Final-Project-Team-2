package com.classpick.web.account.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherLecture {
	private List<DueToLecture> dueToLecture;
	private List<IngLecture> ingLecture;
	private List<EndLecture> endLecture;
}
