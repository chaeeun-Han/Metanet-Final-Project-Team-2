package com.classpick.web.admin.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDashboard {
	private List<MemDashboard> memDashboard;
	private List<LectureDashboard> lectureDashboard;
	private List<PercentDashboard> percentDashboard;
	private List<StudentTeacherDashboard> studentteacherDashboard;
}
