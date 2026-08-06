package com.example.survdemo.application;

import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;

import java.time.LocalDate;
import java.util.List;

public interface MonthlyBatchDataPort {

    List<MonthlyBatchEntitlement> findEligible(LocalDate calculationDate);

    boolean paymentExists(ClaimId claimId, BeneficiaryId beneficiaryId, LocalDate benefitMonth);

    void savePayment(MonthlyBatchPayment payment);

    int updateEntitlement(MonthlyBatchEntitlementUpdate update);

    void saveException(MonthlyBatchException exception);

    void completeRun(String runId, MonthlyBatchTotals totals);
}