package com.example.survdemo.api;

import com.example.survdemo.application.SurvivorInquiryService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/survivor-entitlements", produces = MediaType.APPLICATION_JSON_VALUE)
public class SurvivorInquiryController {

    private final SurvivorInquiryService inquiryService;

    public SurvivorInquiryController(SurvivorInquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping("/{claimId}/beneficiaries/{beneficiaryId}")
    @PreAuthorize("hasAuthority('SCOPE_survivor.inquiry')")
    public SurvivorEntitlementResponse inquire(
            @PathVariable String claimId,
            @PathVariable String beneficiaryId) {
        return SurvivorEntitlementResponse.from(inquiryService.inquire(claimId, beneficiaryId));
    }
}