package com.warbox.dto;

import java.time.OffsetDateTime;

public class InitiateUploadResponse {

    private String videoId;
    private String uploadId;
    private Long chunkSizeBytes;
    private Integer totalChunks;
    private String status;
    private OffsetDateTime expiresAt;

    public InitiateUploadResponse(String videoId, String uploadId, Long chunkSizeBytes, Integer totalChunks, String status, OffsetDateTime expiresAt) {
        this.videoId = videoId;
        this.uploadId = uploadId;
        this.chunkSizeBytes = chunkSizeBytes;
        this.totalChunks = totalChunks;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public Long getChunkSizeBytes() {
        return chunkSizeBytes;
    }

    public void setChunkSizeBytes(Long chunkSizeBytes) {
        this.chunkSizeBytes = chunkSizeBytes;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
