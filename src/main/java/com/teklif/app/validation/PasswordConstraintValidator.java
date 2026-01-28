package com.teklif.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    // Password must be at least 8 characters, contain uppercase, lowercase, digit, and special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?_\\-~.,])(?=\\S+$).{8,}$"
    );

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                            "one lowercase letter, one digit, and one special character (@#$%^&+=!?_-~.,)"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
