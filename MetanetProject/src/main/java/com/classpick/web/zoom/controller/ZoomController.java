package com.classpick.web.zoom.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classpick.web.common.response.ResponseCode;
import com.classpick.web.common.response.ResponseDto;
import com.classpick.web.common.response.ResponseMessage;
import com.classpick.web.util.GetAuthenUser;
import com.classpick.web.zoom.model.ZoomMeetingRequest;
import com.classpick.web.zoom.model.ZoomTokenResponse;
import com.classpick.web.zoom.service.ZoomService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/zoom")
public class ZoomController {

    @Value("${zoom.CLIENT_ID}")
    private String CLIENT_ID;

    @Value("${zoom.REDIRECT_URI}")
    private String REDIRECT_URI;

    private static final String ZOOM_AUTH_URL = "https://zoom.us/oauth/authorize";

    @Autowired
    public ZoomService zoomService;


    // Zoom 인증 페이지로 리다이렉트
    @GetMapping("/auth")
    public ResponseEntity<Void> redirectToZoomAuth() {
        String authUrl = String.format("%s?response_type=code&client_id=%s&redirect_uri=%s",
                ZOOM_AUTH_URL, CLIENT_ID, REDIRECT_URI);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", authUrl);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);  // 302 Redirect
    }


    // Zoom 인증 후 콜백 (프론트에서 처리 필요)
    @GetMapping("/oauth2/callback")
    public String googleAsync(HttpServletRequest req, @RequestParam String code) {
        return code;
    }


    // Zoom Access Token 요청
    @PostMapping("/sign-in")
    public ResponseEntity<ResponseDto> requestZoomAccessToken(@RequestParam String code) {
        String memberId = GetAuthenUser.getAuthenUser();
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }

        ZoomTokenResponse tokenResponse = zoomService.requestZoomAccessToken(code, memberId);
        return ResponseEntity.ok(new ResponseDto<>(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, tokenResponse));
    }


    // Zoom Refresh Token으로 Access Token 갱신
    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseDto> refreshZoomAccessToken() {
        String memberId = GetAuthenUser.getAuthenUser();
        
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }
        Long memberUID = zoomService.getMemberUID(memberId);

        ZoomTokenResponse tokenResponse = zoomService.refreshZoomToken(memberUID);
        return ResponseEntity.ok(new ResponseDto<>(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, tokenResponse));
    }
    
    // zoom 회의실 생성
    @PostMapping("/{lectureId}/meetings")
    public ResponseEntity<ResponseDto> createMeetings(@PathVariable("lectureId") Long lectureId, @RequestBody ZoomMeetingRequest zoomMeetingRequest) {
    	String memberId = GetAuthenUser.getAuthenUser();
        if (memberId == null) {
            return ResponseDto.noAuthentication();
        }
        Long memberUID = zoomService.getMemberUID(memberId);
        
        ResponseEntity<ResponseDto> response = zoomService.createMeeting(memberUID, zoomMeetingRequest);
        return response;
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<ResponseDto> handleZoomWebhook(@RequestBody Map<String, Object> payload) {
        log.info("📩 Zoom Webhook Received: {}", payload);

        String event = (String) payload.get("event");
        Map<String, Object> zoomData = (Map<String, Object>) ((Map<String, Object>) payload.get("payload")).get("object");

        if ("meeting.participant_left".equals(event)) {
            String hostId = (String) zoomData.get("host_id"); 
            Map<String, Object> participant = (Map<String, Object>) zoomData.get("participant");
            String participantId = (String) participant.get("id");

            if (hostId.equals(participantId)) {
                log.info("호스트가 회의에서 나갔습니다. hostId: {}", hostId);
            }
            return zoomService.registerAttendance((String) zoomData.get("topic"), (String) participant.get("email"), (String) participant.get("leave_time"));
        }

        return ResponseDto.serverError();
    }
}