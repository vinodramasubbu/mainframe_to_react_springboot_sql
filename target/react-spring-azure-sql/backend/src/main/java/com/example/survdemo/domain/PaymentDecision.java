package com.example.survdemo.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record PaymentDecision(
        ClaimId claimId,
        BeneficiaryId beneficiaryId,
        BigDecimal grossAmount,
        BigDecimal netAmount,
        String status) implements MonthlyCalculationDecision {

    public PaymentDecision {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
        Objects.requireNonNull(grossAmount, "grossAmount must not be null");
        Objects.requireNonNull(netAmount, "netAmount must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}