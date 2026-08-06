package com.example.survdemo.application;

import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;

import java.math.BigDecimal;
import java.util.Objects;

public record MonthlyBatchException(
        String runId,
        ClaimId claimId,
        BeneficiaryId beneficiaryId,
        String reasonCode,
        BigDecimal expectedAmount,
        BigDecimal actualAmount) {

    public MonthlyBatchException {
        Objects.requireNonNull(runId, "runId must not be null");
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
        Objects.requireNonNull(reasonCode, "reasonCode must not be null");
        Objects.requireNonNull(expectedAmount, "expectedAmount must not be null");
        Objects.requireNonNull(actualAmount, "actualAmount must not be null");
    }
}