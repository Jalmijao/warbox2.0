package com.warbox.controller;

import com.warbox.dto.InitiateUploadRequest;
import com.warbox.dto.InitiateUploadResponse;
import com.warbox.service.VideoUploadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/videos", produces = MediaType.APPLICATION_JSON_VALUE)
public class VideoUploadController {

    private final VideoUploadService uploadService;

    public VideoUploadController(VideoUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(path = "/initiate-upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InitiateUploadResponse> initiateUpload(@Valid @RequestBody InitiateUploadRequest request) {
        InitiateUploadResponse response = uploadService.initiateUpload(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
