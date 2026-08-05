package com.example.survdemo.domain;

public record BeneficiaryId(String value) {

    public BeneficiaryId {
        if (value == null || value.isBlank()) {
            throw new InvalidInquiryIdentifierException("Beneficiary ID is required");
        }
        if (!value.equals(value.strip())) {
            throw new InvalidInquiryIdentifierException("Beneficiary ID cannot have leading or trailing spaces");
        }
        if (value.length() > 10) {
            throw new InvalidInquiryIdentifierException("Beneficiary ID cannot exceed 10 characters");
        }
    }
}