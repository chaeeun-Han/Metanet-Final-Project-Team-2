package com.example.myapp.board.model;

import java.sql.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class S3Images {
    private long imageId;       
    private String imageUrl;
    private Date imageCreated;
    private Date imageUpdated;
    private boolean deleted;
}

