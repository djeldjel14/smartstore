package com.smartstore.backend.dto;

import java.time.LocalDateTime;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    Long parentId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}