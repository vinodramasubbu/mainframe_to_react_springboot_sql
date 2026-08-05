package com.example.survdemo.application;

import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import com.example.survdemo.domain.EntitlementPresentation;
import com.example.survdemo.domain.SurvivorEntitlementView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SurvivorInquiryService {

    private final SurvivorEntitlementRepository repository;

    public SurvivorInquiryService(SurvivorEntitlementRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SurvivorEntitlementView inquire(String claimId, String beneficiaryId) {
        ClaimId validatedClaimId = new ClaimId(claimId);
        BeneficiaryId validatedBeneficiaryId = new BeneficiaryId(beneficiaryId);
        return repository.find(validatedClaimId, validatedBeneficiaryId)
                .map(EntitlementPresentation::present)
                .orElseThrow(EntitlementNotFoundException::new);
    }
}