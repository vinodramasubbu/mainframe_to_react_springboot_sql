package com.example.survdemo.domain;

import java.time.LocalDate;

public record SurvivorEntitlementView(
        String claimId,
        String beneficiaryId,
        String beneficiaryName,
        String relationshipCode,
        String relationshipLabel,
        String monthlyAmount,
        LocalDate startDate,
        LocalDate endDate,
        String displayStatus,
        String message) {
}