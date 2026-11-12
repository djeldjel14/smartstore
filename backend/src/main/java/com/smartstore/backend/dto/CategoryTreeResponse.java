package com.smartstore.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryTreeResponse(
    Long id,
    String name,
    String description,
    Long parentId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<CategoryTreeResponse> children
) {}