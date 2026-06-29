package com.warbox.service;

import com.warbox.dto.InitiateUploadRequest;
import com.warbox.dto.InitiateUploadResponse;

public interface VideoUploadService {

    InitiateUploadResponse initiateUpload(InitiateUploadRequest request);
}
