package com.smartstore.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UnicodeCodePointLengthValidator
        implements ConstraintValidator<UnicodeCodePointLength, CharSequence> {

    private int max;
    private int min;

    @Override
    public void initialize(UnicodeCodePointLength constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return min == 0;
        }

        String normalized = UnicodeText.trimWhitespace(value.toString());
        int codePointLength = CategoryTextRules.codePointLength(normalized);
        return codePointLength >= min && codePointLength <= max;
    }
}
