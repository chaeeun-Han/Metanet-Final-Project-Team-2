package com.classpick.web.aop;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Log {
	private Long logId;
	private String requestUrl;
	private String requestMethod;
	private String clientIp;
	private LocalDateTime requestTime;
	private Long executionTime;
	private String serviceName;
}
