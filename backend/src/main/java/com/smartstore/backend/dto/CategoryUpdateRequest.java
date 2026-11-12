package com.smartstore.backend.dto;

import com.smartstore.backend.validation.CategoryTextRules;
import com.smartstore.backend.validation.UnicodeCodePointLength;

public record CategoryUpdateRequest(
    @UnicodeCodePointLength(min = CategoryTextRules.NAME_MIN_CODE_POINTS,
            max = CategoryTextRules.NAME_MAX_CODE_POINTS,
            message = "Category name must contain between 1 and 100 Unicode code points.")
    String name,

    @UnicodeCodePointLength(max = CategoryTextRules.DESCRIPTION_MAX_CODE_POINTS,
            message = "Description must not exceed 255 Unicode code points.")
    String description,

    Long parentId
) {}