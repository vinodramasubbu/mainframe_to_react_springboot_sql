package com.example.survdemo.domain;

import java.math.BigDecimal;
import java.util.Set;

public final class SurvivorValidationService {

    private static final Set<String> VALID_RELATIONSHIPS = Set.of("SPS", "CHD", "DEP");
    private static final BigDecimal MAX_PERCENTAGE = new BigDecimal("100");

    public ValidationResult validate(ValidationInput input) {
        if (!"A".equals(input.claimStatus())) {
            return ValidationResult.invalid("C1");
        }
        if (!"A".equals(input.beneficiaryStatus())) {
            return ValidationResult.invalid("B1");
        }
        if (!VALID_RELATIONSHIPS.contains(input.relationship())) {
            return ValidationResult.invalid("R1");
        }
        if (input.benefitPercentage().signum() <= 0
                || input.benefitPercentage().compareTo(MAX_PERCENTAGE) > 0) {
            return ValidationResult.invalid("P1");
        }
        if (input.otherIncomeOffset().signum() < 0) {
            return ValidationResult.invalid("O1");
        }
        if (input.baseMonthlyBenefit().signum() <= 0) {
            return ValidationResult.invalid("A1");
        }
        return ValidationResult.accepted();
    }
}