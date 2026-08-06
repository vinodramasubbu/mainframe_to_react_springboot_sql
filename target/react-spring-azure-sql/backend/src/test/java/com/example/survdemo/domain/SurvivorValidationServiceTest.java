package com.example.survdemo.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SurvivorValidationServiceTest {

    private final SurvivorValidationService service = new SurvivorValidationService();

    @Test
    void acceptsValidEntitlement() {
        assertThat(service.validate(validInput()))
                .isEqualTo(ValidationResult.accepted());
    }

    @Test
    void returnsFirstFailureInLegacyPriorityOrder() {
        assertInvalid(input("I", "I", "BAD", "0.00", "-1.00", "0.00"), "C1");
        assertInvalid(input("A", "I", "BAD", "0.00", "-1.00", "0.00"), "B1");
        assertInvalid(input("A", "A", "BAD", "0.00", "-1.00", "0.00"), "R1");
        assertInvalid(input("A", "A", "SPS", "0.00", "-1.00", "0.00"), "P1");
        assertInvalid(input("A", "A", "SPS", "100.01", "-1.00", "0.00"), "P1");
        assertInvalid(input("A", "A", "SPS", "50.00", "-0.01", "0.00"), "O1");
        assertInvalid(input("A", "A", "SPS", "50.00", "0.00", "0.00"), "A1");
    }

    @Test
    void acceptsEveryLegacyRelationshipAndPercentageBoundary() {
        assertThat(service.validate(input("A", "A", "SPS", "0.01", "0.00", "1.00")).valid()).isTrue();
        assertThat(service.validate(input("A", "A", "CHD", "50.00", "0.00", "1.00")).valid()).isTrue();
        assertThat(service.validate(input("A", "A", "DEP", "100.00", "0.00", "1.00")).valid()).isTrue();
    }

    private void assertInvalid(ValidationInput input, String expectedReasonCode) {
        assertThat(service.validate(input))
                .isEqualTo(ValidationResult.invalid(expectedReasonCode));
    }

    private ValidationInput validInput() {
        return input("A", "A", "SPS", "50.00", "0.00", "1000.00");
    }

    private ValidationInput input(
            String claimStatus,
            String beneficiaryStatus,
            String relationship,
            String benefitPercentage,
            String otherIncomeOffset,
            String baseMonthlyBenefit) {
        return new ValidationInput(
                claimStatus,
                beneficiaryStatus,
                relationship,
                new BigDecimal(benefitPercentage),
                new BigDecimal(otherIncomeOffset),
                new BigDecimal(baseMonthlyBenefit));
    }
}