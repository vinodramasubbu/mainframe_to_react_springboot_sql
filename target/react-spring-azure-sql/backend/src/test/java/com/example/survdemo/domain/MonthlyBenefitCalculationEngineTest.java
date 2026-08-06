package com.example.survdemo.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyBenefitCalculationEngineTest {

    private final MonthlyBenefitCalculationEngine engine = new MonthlyBenefitCalculationEngine();

    @Test
    void roundsGrossAmountBeforeSubtractingOffset() {
        PaymentDecision decision = payment(engine.calculate(List.of(
                input("CLM000000001", "BENE000001", "1.00", "100.00", "50.50", "0.01", false))).get(0));

        assertThat(decision.grossAmount()).isEqualByComparingTo("0.51");
        assertThat(decision.netAmount()).isEqualByComparingTo("0.50");
        assertThat(decision.status()).isEqualTo("R");
    }

    @Test
    void preservesRegularStatusAtExactFamilyCapBoundary() {
        List<MonthlyCalculationDecision> decisions = engine.calculate(List.of(
                input("CLM000000001", "BENE000002", "1000.00", "100.00", "50.00", "0.00", false),
                input("CLM000000001", "BENE000001", "1000.00", "100.00", "50.00", "0.00", false)));

        assertThat(decisions).extracting(decision -> decision.beneficiaryId().value())
                .containsExactly("BENE000001", "BENE000002");
        assertThat(payment(decisions.get(0)).status()).isEqualTo("R");
        assertThat(payment(decisions.get(1)).status()).isEqualTo("R");
        assertThat(payment(decisions.get(1)).netAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void allocatesInBeneficiaryOrderReducesCapCrossingPaymentAndRejectsLaterPayment() {
        List<MonthlyCalculationDecision> decisions = engine.calculate(List.of(
                input("CLM000000001", "BENE000003", "1000.00", "100.00", "60.00", "0.00", false),
                input("CLM000000001", "BENE000001", "1000.00", "100.00", "60.00", "0.00", false),
                input("CLM000000001", "BENE000002", "1000.00", "100.00", "60.00", "0.00", false)));

        assertThat(decisions).extracting(decision -> decision.beneficiaryId().value())
                .containsExactly("BENE000001", "BENE000002", "BENE000003");
        assertThat(payment(decisions.get(0)).netAmount()).isEqualByComparingTo("600.00");
        assertThat(payment(decisions.get(0)).status()).isEqualTo("R");
        assertThat(payment(decisions.get(1)).netAmount()).isEqualByComparingTo("400.00");
        assertThat(payment(decisions.get(1)).status()).isEqualTo("C");
        assertThat(exception(decisions.get(2)).reasonCode()).isEqualTo("F1");
    }

    @Test
    void reportsZeroOrNegativeNetWithCalculatedAmounts() {
        ExceptionDecision zero = exception(engine.calculate(List.of(
                input("CLM000000001", "BENE000001", "1000.00", "100.00", "10.00", "100.00", false))).get(0));
        ExceptionDecision negative = exception(engine.calculate(List.of(
                input("CLM000000001", "BENE000001", "1000.00", "100.00", "10.00", "100.01", false))).get(0));

        assertThat(zero.reasonCode()).isEqualTo("Z1");
        assertThat(zero.grossAmount()).isEqualByComparingTo("100.00");
        assertThat(zero.netAmount()).isEqualByComparingTo("0.00");
        assertThat(negative.reasonCode()).isEqualTo("Z1");
        assertThat(negative.netAmount()).isEqualByComparingTo("-0.01");
    }

    @Test
    void validationRunsBeforeDuplicateCheckAndDuplicateAmountsAreZero() {
        MonthlyCalculationInput invalidDuplicate = new MonthlyCalculationInput(
                new ClaimId("CLM000000001"),
                new BeneficiaryId("BENE000001"),
                validation("I", "A", "SPS", "1000.00", "50.00", "0.00"),
                new BigDecimal("100.00"),
                true);
        ExceptionDecision validationFailure = exception(engine.calculate(List.of(invalidDuplicate)).get(0));
        ExceptionDecision duplicate = exception(engine.calculate(List.of(
                input("CLM000000001", "BENE000001", "1000.00", "100.00", "50.00", "0.00", true))).get(0));

        assertThat(validationFailure.reasonCode()).isEqualTo("C1");
        assertThat(duplicate.reasonCode()).isEqualTo("D1");
        assertThat(duplicate.grossAmount()).isEqualByComparingTo("0.00");
        assertThat(duplicate.netAmount()).isEqualByComparingTo("0.00");
    }

    private MonthlyCalculationInput input(
            String claimId,
            String beneficiaryId,
            String baseAmount,
            String familyMaximumPercentage,
            String benefitPercentage,
            String offsetAmount,
            boolean duplicatePayment) {
        return new MonthlyCalculationInput(
                new ClaimId(claimId),
                new BeneficiaryId(beneficiaryId),
                validation("A", "A", "SPS", baseAmount, benefitPercentage, offsetAmount),
                new BigDecimal(familyMaximumPercentage),
                duplicatePayment);
    }

    private ValidationInput validation(
            String claimStatus,
            String beneficiaryStatus,
            String relationship,
            String baseAmount,
            String benefitPercentage,
            String offsetAmount) {
        return new ValidationInput(
                claimStatus,
                beneficiaryStatus,
                relationship,
                new BigDecimal(benefitPercentage),
                new BigDecimal(offsetAmount),
                new BigDecimal(baseAmount));
    }

    private PaymentDecision payment(MonthlyCalculationDecision decision) {
        assertThat(decision).isInstanceOf(PaymentDecision.class);
        return (PaymentDecision) decision;
    }

    private ExceptionDecision exception(MonthlyCalculationDecision decision) {
        assertThat(decision).isInstanceOf(ExceptionDecision.class);
        return (ExceptionDecision) decision;
    }
}