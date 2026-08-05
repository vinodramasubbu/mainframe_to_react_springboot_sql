package com.example.survdemo.domain;

public record ClaimId(String value) {

    public ClaimId {
        value = validate(value, 12, "Claim ID");
    }

    private static String validate(String value, int maximumLength, String label) {
        if (value == null || value.isBlank()) {
            throw new InvalidInquiryIdentifierException(label + " is required");
        }
        if (!value.equals(value.strip())) {
            throw new InvalidInquiryIdentifierException(label + " cannot have leading or trailing spaces");
        }
        if (value.length() > maximumLength) {
            throw new InvalidInquiryIdentifierException(label + " cannot exceed " + maximumLength + " characters");
        }
        return value;
    }
}