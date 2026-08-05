package com.example.survdemo.application;

import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import com.example.survdemo.domain.SurvivorEntitlement;

import java.util.Optional;

public interface SurvivorEntitlementRepository {

    Optional<SurvivorEntitlement> find(ClaimId claimId, BeneficiaryId beneficiaryId);
}