package com.smartstore.backend.validation;

import com.smartstore.backend.dto.CategoryCreateRequest;
import com.smartstore.backend.dto.CategoryUpdateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnicodeCodePointLengthTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void createRequest_AcceptsLimitsAfterTrimmingSupplementaryCharacters() {
        CategoryCreateRequest request = new CategoryCreateRequest(
                "\u00A0" + "🍎".repeat(100) + "\u202F",
                "\u0085" + "🍎".repeat(255) + "\u3000",
                null
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void createRequest_RejectsValuesOverCodePointLimits() {
        CategoryCreateRequest request = new CategoryCreateRequest(
                "🍎".repeat(101),
                "🍎".repeat(256),
                null
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "name",
                        "description"
                );
    }

    @Test
    void createRequest_RejectsUnicodeWhitespaceOnlyNames() {
        CategoryCreateRequest request = new CategoryCreateRequest("\u00A0\u202F\u0085", null, null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("name");
    }

    @Test
    void updateRequest_UsesUnicodeCodePointLengthValidation() {
        CategoryUpdateRequest valid = new CategoryUpdateRequest("🍎".repeat(100), null, null);
        CategoryUpdateRequest invalid = new CategoryUpdateRequest("🍎".repeat(101), null, null);

        assertThat(validator.validate(valid)).isEmpty();
        assertThat(validator.validate(invalid))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("name");
    }
}
