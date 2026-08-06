package com.example.survdemo.domain;

import java.math.BigDecimal;

public sealed interface MonthlyCalculationDecision permits PaymentDecision, ExceptionDecision {

    ClaimId claimId();

    BeneficiaryId beneficiaryId();

    BigDecimal grossAmount();

    BigDecimal netAmount();
}