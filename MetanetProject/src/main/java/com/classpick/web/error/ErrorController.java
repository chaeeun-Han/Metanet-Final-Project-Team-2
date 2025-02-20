package com.classpick.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.classpick.web.common.response.ResponseDto;
@Controller
@RequestMapping("/api/access-denied")
public class ErrorController {
    
    @GetMapping("/{errorCode}")
    public ResponseEntity<ResponseDto> errorCode(@PathVariable String errorCode) {
        ResponseDto responseBody = new ResponseDto(errorCode);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        switch (errorCode) {
        case "400":
            status = HttpStatus.BAD_REQUEST;
            break;
        case "403":
            status = HttpStatus.FORBIDDEN;
            break;
        case "500":
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            break;
        }

        return ResponseEntity.status(status).body(responseBody);
    }
}
