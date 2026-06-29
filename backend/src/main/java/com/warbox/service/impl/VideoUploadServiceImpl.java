package com.warbox.service.impl;

import com.warbox.config.S3Config;
import com.warbox.dto.InitiateUploadRequest;
import com.warbox.dto.InitiateUploadResponse;
import com.warbox.service.VideoUploadService;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class VideoUploadServiceImpl implements VideoUploadService {

    private final S3Client s3Client;
    private final S3Config s3Config;

    public VideoUploadServiceImpl(S3Client s3Client, S3Config s3Config) {
        this.s3Client = s3Client;
        this.s3Config = s3Config;
    }

    @Override
    public InitiateUploadResponse initiateUpload(InitiateUploadRequest request) {
        long chunkSizeBytes = request.getChunkSizeBytes() != null ? request.getChunkSizeBytes() : s3Config.getDefaultChunkSizeBytes();
        if (chunkSizeBytes <= 0) {
            chunkSizeBytes = s3Config.getDefaultChunkSizeBytes();
        }

        long totalChunks = (request.getFileSizeBytes() + chunkSizeBytes - 1) / chunkSizeBytes;
        String videoId = UUID.randomUUID().toString();
        String key = String.format("videos/%s", videoId);

        CreateMultipartUploadRequest uploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(s3Config.getBucket())
                .key(key)
                .contentType("video/mp4")
                .metadata(requestMetadata(request))
                .build();

        CreateMultipartUploadResponse uploadResponse = s3Client.createMultipartUpload(uploadRequest);

        return new InitiateUploadResponse(
                videoId,
                uploadResponse.uploadId(),
                chunkSizeBytes,
                Math.toIntExact(totalChunks),
                "awaiting_chunks",
                OffsetDateTime.now().plusHours(1)
        );
    }

    private java.util.Map<String, String> requestMetadata(InitiateUploadRequest request) {
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        metadata.put("title", request.getTitle());
        metadata.put("description", request.getDescription());
        metadata.put("category", request.getCategory());
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            metadata.put("tags", String.join(",", request.getTags()));
        }
        metadata.put("fileSizeBytes", request.getFileSizeBytes().toString());
        return metadata;
    }
}
