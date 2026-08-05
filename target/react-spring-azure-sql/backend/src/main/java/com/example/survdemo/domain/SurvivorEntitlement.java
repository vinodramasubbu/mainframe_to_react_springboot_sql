package com.example.survdemo.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SurvivorEntitlement(
        ClaimId claimId,
        BeneficiaryId beneficiaryId,
        String beneficiaryName,
        String relationshipCode,
        String beneficiaryStatus,
        String claimStatus,
        String entitlementStatus,
        BigDecimal monthlyAmount,
        LocalDate startDate,
        LocalDate endDate) {
}