package com.example.survdemo.application;

import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record MonthlyBatchPayment(
        String paymentId,
        String runId,
        ClaimId claimId,
        BeneficiaryId beneficiaryId,
        LocalDate benefitMonth,
        BigDecimal grossAmount,
        BigDecimal offsetAmount,
        BigDecimal netAmount,
        String status) {

    public MonthlyBatchPayment {
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(runId, "runId must not be null");
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
        Objects.requireNonNull(benefitMonth, "benefitMonth must not be null");
        Objects.requireNonNull(grossAmount, "grossAmount must not be null");
        Objects.requireNonNull(offsetAmount, "offsetAmount must not be null");
        Objects.requireNonNull(netAmount, "netAmount must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}