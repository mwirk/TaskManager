package com.taskmanager.org.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {


    private static final String PASSWORD_PATTERN =
            "^(?=.*\\d)(?=.*[@#$%^&+=!]).+$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true;
        }
        return password.matches(PASSWORD_PATTERN);
    }
}
