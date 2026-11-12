package com.smartstore.backend.validation;

public final class CategoryTextRules {

    public static final int NAME_MIN_CODE_POINTS = 1;
    public static final int NAME_MAX_CODE_POINTS = 100;
    public static final int DESCRIPTION_MAX_CODE_POINTS = 255;

    private CategoryTextRules() {
    }

    public static int codePointLength(String value) {
        return value.codePointCount(0, value.length());
    }
}
