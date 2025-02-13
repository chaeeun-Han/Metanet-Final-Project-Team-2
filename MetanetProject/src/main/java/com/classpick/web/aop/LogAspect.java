package com.classpick.web.aop;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {
   
   private final IAopRepository aopRepository;
   
   @Around("execution(* com.classpick.web..*Service.*(..))")
   public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
      
      String className = joinPoint.getTarget().getClass().getSimpleName();
      String methodName = joinPoint.getSignature().getName();
        LocalDateTime requestTime = LocalDateTime.now();
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String url = request == null ? "N/A" : request.getRequestURL().toString();
        String httpMethod = request == null ? "N/A" : request.getMethod();
        String clientIp = request == null ? "N/A" : getRemoteAddr(request);
        
        log.info("[[[AOP-before log]]]-{}: Request to URL '{}' with HTTP Method '{}' from IP '{}'", methodName, url, httpMethod, clientIp);
        
        Object result;
        long startTime = System.currentTimeMillis();
        long executionTime;
        
        try {
            result = joinPoint.proceed();
            executionTime = System.currentTimeMillis() - startTime;

            log.info("[[[AOP-after log]]]-{}: Method executed successfully", methodName);

        } catch (Throwable throwable) {
            executionTime = System.currentTimeMillis() - startTime;
            log.error("[[[AOP-exception log]]]-{}: Exception occurred: {}", methodName, throwable.getMessage());
            throw throwable;
        }

        
        Log logEntry = new Log();
        logEntry.setRequestUrl(url);
        logEntry.setRequestMethod(httpMethod);
        logEntry.setClientIp(clientIp);
        logEntry.setRequestTime(requestTime);
        logEntry.setExecutionTime(executionTime);
        logEntry.setServiceName(className + "." + methodName);

        log.info("Log : {}", logEntry);
        aopRepository.insertLog(logEntry);

        return result;
   }
   
   public static String getRemoteAddr(HttpServletRequest request) {
       String ip = request.getHeader("X-Forwarded-For");

       if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
           return ip.split(",")[0].trim();
       }

       ip = request.getHeader("Proxy-Client-IP");
       if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;

       ip = request.getHeader("WL-Proxy-Client-IP");
       if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;

       ip = request.getHeader("HTTP_CLIENT_IP");
       if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;

       ip = request.getHeader("HTTP_X_FORWARDED_FOR");
       if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;

       ip = request.getHeader("X-Real-IP");
       if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;

       ip = request.getHeader("X-RealIP");
       if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;

       ip = request.getHeader("REMOTE_ADDR");
       if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) return ip;

       ip = request.getRemoteAddr();

       if ("0:0:0:0:0:0:0:1".equals(ip)) {
           ip = "127.0.0.1";
       }

       return ip;
   }
}
