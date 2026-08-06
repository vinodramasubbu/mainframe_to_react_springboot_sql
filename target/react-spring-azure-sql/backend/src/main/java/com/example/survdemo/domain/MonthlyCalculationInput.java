package com.example.survdemo.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record MonthlyCalculationInput(
        ClaimId claimId,
        BeneficiaryId beneficiaryId,
        ValidationInput validationInput,
        BigDecimal familyMaximumPercentage,
        boolean duplicatePayment) {

    public MonthlyCalculationInput {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
        Objects.requireNonNull(validationInput, "validationInput must not be null");
        Objects.requireNonNull(familyMaximumPercentage, "familyMaximumPercentage must not be null");
    }
}