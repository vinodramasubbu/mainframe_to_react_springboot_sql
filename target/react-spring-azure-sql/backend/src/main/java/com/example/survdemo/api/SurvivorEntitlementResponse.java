package com.example.survdemo.api;

import com.example.survdemo.domain.SurvivorEntitlementView;

import java.time.LocalDate;

public record SurvivorEntitlementResponse(
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

    public static SurvivorEntitlementResponse from(SurvivorEntitlementView view) {
        return new SurvivorEntitlementResponse(
                view.claimId(),
                view.beneficiaryId(),
                view.beneficiaryName(),
                view.relationshipCode(),
                view.relationshipLabel(),
                view.monthlyAmount(),
                view.startDate(),
                view.endDate(),
                view.displayStatus(),
                view.message());
    }
}