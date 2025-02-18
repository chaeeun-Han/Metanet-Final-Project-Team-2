package com.classpick.web.lecture.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LectureList {
    private Long lectureListId;
    private String date;
    private Long lectureId;
    private Long memberId;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String link;
    private int meetingId;
    private String name;
}
