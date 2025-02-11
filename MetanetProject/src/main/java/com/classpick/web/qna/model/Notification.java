package com.classpick.web.qna.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Notification {
   private String message;
   private LocalDateTime timestamp;
   private Long lectureId;
   private Long questionId;
}
