package com.example.survdemo.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record ExceptionDecision(
        ClaimId claimId,
        BeneficiaryId beneficiaryId,
        BigDecimal grossAmount,
        BigDecimal netAmount,
        String reasonCode) implements MonthlyCalculationDecision {

    public ExceptionDecision {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
        Objects.requireNonNull(grossAmount, "grossAmount must not be null");
        Objects.requireNonNull(netAmount, "netAmount must not be null");
        Objects.requireNonNull(reasonCode, "reasonCode must not be null");
    }
}