package com.example.survdemo.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class MonthlyBenefitCalculationEngine {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");
    private static final int MONEY_SCALE = 2;

    private final SurvivorValidationService validationService;

    public MonthlyBenefitCalculationEngine() {
        this(new SurvivorValidationService());
    }

    MonthlyBenefitCalculationEngine(SurvivorValidationService validationService) {
        this.validationService = Objects.requireNonNull(validationService);
    }

    public List<MonthlyCalculationDecision> calculate(List<MonthlyCalculationInput> inputs) {
        List<MonthlyCalculationInput> orderedInputs = inputs.stream()
                .sorted(Comparator.comparing((MonthlyCalculationInput input) -> input.claimId().value())
                        .thenComparing(input -> input.beneficiaryId().value()))
                .toList();
        List<MonthlyCalculationDecision> decisions = new ArrayList<>(orderedInputs.size());

        ClaimId currentClaimId = null;
        BigDecimal familyTotal = ZERO_AMOUNT;
        BigDecimal familyCap = ZERO_AMOUNT;

        for (MonthlyCalculationInput input : orderedInputs) {
            if (!input.claimId().equals(currentClaimId)) {
                currentClaimId = input.claimId();
                familyTotal = ZERO_AMOUNT;
                familyCap = percentageOf(
                        input.validationInput().baseMonthlyBenefit(),
                        input.familyMaximumPercentage());
            }

            ValidationResult validation = validationService.validate(input.validationInput());
            if (!validation.valid()) {
                decisions.add(exception(input, ZERO_AMOUNT, ZERO_AMOUNT, validation.reasonCode()));
                continue;
            }
            if (input.duplicatePayment()) {
                decisions.add(exception(input, ZERO_AMOUNT, ZERO_AMOUNT, "D1"));
                continue;
            }

            BigDecimal grossAmount = percentageOf(
                    input.validationInput().baseMonthlyBenefit(),
                    input.validationInput().benefitPercentage());
            BigDecimal netAmount = grossAmount.subtract(input.validationInput().otherIncomeOffset());
            if (netAmount.signum() <= 0) {
                decisions.add(exception(input, grossAmount, netAmount, "Z1"));
                continue;
            }
            if (familyTotal.compareTo(familyCap) >= 0) {
                decisions.add(exception(input, grossAmount, netAmount, "F1"));
                continue;
            }

            String status = "R";
            if (familyTotal.add(netAmount).compareTo(familyCap) > 0) {
                netAmount = familyCap.subtract(familyTotal);
                status = "C";
            }
            decisions.add(new PaymentDecision(
                    input.claimId(), input.beneficiaryId(), grossAmount, netAmount, status));
            familyTotal = familyTotal.add(netAmount);
        }

        return List.copyOf(decisions);
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage)
                .divide(ONE_HUNDRED)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private ExceptionDecision exception(
            MonthlyCalculationInput input,
            BigDecimal grossAmount,
            BigDecimal netAmount,
            String reasonCode) {
        return new ExceptionDecision(
                input.claimId(), input.beneficiaryId(), grossAmount, netAmount, reasonCode);
    }
}