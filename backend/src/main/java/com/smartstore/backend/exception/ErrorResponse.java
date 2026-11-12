package com.smartstore.backend.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String code,
    String message,
    String path,
    List<FieldErrorDetails> fieldErrors
) {
    public record FieldErrorDetails(String field, String code, String message) {}
}