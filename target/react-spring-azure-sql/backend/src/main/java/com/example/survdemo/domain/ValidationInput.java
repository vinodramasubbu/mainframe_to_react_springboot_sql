package com.example.survdemo.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record ValidationInput(
        String claimStatus,
        String beneficiaryStatus,
        String relationship,
        BigDecimal benefitPercentage,
        BigDecimal otherIncomeOffset,
        BigDecimal baseMonthlyBenefit) {

    public ValidationInput {
        Objects.requireNonNull(claimStatus, "claimStatus must not be null");
        Objects.requireNonNull(beneficiaryStatus, "beneficiaryStatus must not be null");
        Objects.requireNonNull(relationship, "relationship must not be null");
        Objects.requireNonNull(benefitPercentage, "benefitPercentage must not be null");
        Objects.requireNonNull(otherIncomeOffset, "otherIncomeOffset must not be null");
        Objects.requireNonNull(baseMonthlyBenefit, "baseMonthlyBenefit must not be null");
    }
}