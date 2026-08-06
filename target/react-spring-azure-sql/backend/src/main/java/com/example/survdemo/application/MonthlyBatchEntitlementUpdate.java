package com.example.survdemo.application;

import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;

import java.math.BigDecimal;
import java.util.Objects;

public record MonthlyBatchEntitlementUpdate(
        ClaimId claimId,
        BeneficiaryId beneficiaryId,
        BigDecimal netAmount,
        int expectedVersion) {

    public MonthlyBatchEntitlementUpdate {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(beneficiaryId, "beneficiaryId must not be null");
        Objects.requireNonNull(netAmount, "netAmount must not be null");
    }
}