package com.classpick.web.lecture.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LectureId {
    Long memberId;
    Long lectureId;
}
