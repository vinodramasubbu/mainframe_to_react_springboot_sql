package com.example.survdemo.domain;

import java.util.Objects;

public record ValidationResult(boolean valid, String reasonCode) {

    public ValidationResult {
        Objects.requireNonNull(reasonCode, "reasonCode must not be null");
        if (valid && !reasonCode.isEmpty()) {
            throw new IllegalArgumentException("A valid result cannot have a reason code");
        }
        if (!valid && reasonCode.isEmpty()) {
            throw new IllegalArgumentException("An invalid result must have a reason code");
        }
    }

    public static ValidationResult accepted() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult invalid(String reasonCode) {
        return new ValidationResult(false, reasonCode);
    }
}