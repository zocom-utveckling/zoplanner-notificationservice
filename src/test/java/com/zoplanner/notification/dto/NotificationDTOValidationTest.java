package com.zoplanner.notification.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

public class NotificationDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private NotificationDTO validDTO() {
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage("Hello");
        dto.setRecipient("test@test.com");
        dto.setChannel("EMAIL");
        dto.setChannel("GENERIC");
        return dto;
    }

    @Test
    void testMissingMessage() {
        NotificationDTO dto = validDTO();
        dto.setMessage("");

        Set<ConstraintViolation<NotificationDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Message cannot be empty");
    }

    @Test
    void testMissingRecipient() {
        NotificationDTO dto = validDTO();
        dto.setRecipient("");

        Set<ConstraintViolation<NotificationDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Recipient cannot be empty");

    }

    @Test
    void testInvalidEmail() {
        NotificationDTO dto = validDTO();
        dto.setRecipient("invalid");

        Set<ConstraintViolation<NotificationDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("recipient"))).isTrue();
    }

    @Test
    void testMissingChannel() {
        NotificationDTO dto = validDTO();
        dto.setChannel("");

        Set<ConstraintViolation<NotificationDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("channel"))).isTrue();
    }

    @Test
    void testMissingEventType() {
        NotificationDTO dto = validDTO();
        dto.setEventType("");

        Set<ConstraintViolation<NotificationDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("eventType"))).isTrue();
    }
}
