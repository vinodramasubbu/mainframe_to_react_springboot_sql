package com.example.survdemo.application;

import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;

public final class OptimisticEntitlementUpdateException extends RuntimeException {

    OptimisticEntitlementUpdateException(ClaimId claimId, BeneficiaryId beneficiaryId, int rowsUpdated) {
        super("Expected one active entitlement version to update for " + claimId.value() + "/"
                + beneficiaryId.value() + " but updated " + rowsUpdated);
    }
}