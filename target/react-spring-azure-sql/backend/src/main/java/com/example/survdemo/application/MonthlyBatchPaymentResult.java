package com.example.survdemo.application;

import java.util.Objects;

public record MonthlyBatchPaymentResult(
        MonthlyBatchPayment payment,
        String relationshipCode,
        String beneficiaryName) {

    public MonthlyBatchPaymentResult {
        Objects.requireNonNull(payment, "payment must not be null");
        Objects.requireNonNull(relationshipCode, "relationshipCode must not be null");
        Objects.requireNonNull(beneficiaryName, "beneficiaryName must not be null");
    }
}