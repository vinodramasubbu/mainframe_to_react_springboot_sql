package com.example.survdemo.application;

import com.example.survdemo.domain.MonthlyCalculationInput;

import java.util.Objects;

public record MonthlyBatchEntitlement(
        MonthlyCalculationInput calculationInput,
        String beneficiaryName,
        int versionNumber) {

    public MonthlyBatchEntitlement {
        Objects.requireNonNull(calculationInput, "calculationInput must not be null");
        Objects.requireNonNull(beneficiaryName, "beneficiaryName must not be null");
        if (versionNumber < 1) {
            throw new IllegalArgumentException("versionNumber must be positive");
        }
    }
}