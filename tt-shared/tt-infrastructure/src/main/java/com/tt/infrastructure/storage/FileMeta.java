package com.tt.infrastructure.storage;

public record FileMeta(
    String fileId,
    String originalName,
    String ext,
    String contentType,
    long size,
    long uploadedAtEpochMillis) {}

