package com.example.survdemo.api;

import com.example.survdemo.application.MonthlyBatchException;
import com.example.survdemo.application.MonthlyBatchExecutionResult;
import com.example.survdemo.application.MonthlyBatchPaymentResult;

import java.time.LocalDate;
import java.util.List;

public record MonthlyBenefitRunResponse(
        String runId,
        LocalDate calculationDate,
        LocalDate benefitMonth,
        int returnCode,
        String outcome,
        int paymentCount,
        String paymentTotal,
        int exceptionCount,
        List<Payment> payments,
        List<ExceptionResult> exceptions) {

    static MonthlyBenefitRunResponse from(MonthlyBatchExecutionResult result) {
        return new MonthlyBenefitRunResponse(
                result.command().runId(),
                result.command().calculationDate(),
                result.command().benefitMonth(),
                result.outcome().returnCode(),
                outcomeLabel(result.outcome().returnCode()),
                result.outcome().paymentCount(),
                result.outcome().paymentTotal().toPlainString(),
                result.outcome().exceptionCount(),
                result.payments().stream().map(Payment::from).toList(),
                result.exceptions().stream().map(ExceptionResult::from).toList());
    }

    private static String outcomeLabel(int returnCode) {
        return switch (returnCode) {
            case 0 -> "CLEAN";
            case 4 -> "COMPLETED_WITH_EXCEPTIONS";
            case 12 -> "TECHNICAL_FAILURE";
            default -> throw new IllegalArgumentException("Unsupported monthly batch return code");
        };
    }

    public record Payment(
            String paymentId,
            String claimId,
            String beneficiaryId,
            String beneficiaryName,
            String relationshipCode,
            LocalDate benefitMonth,
            String grossAmount,
            String offsetAmount,
            String netAmount,
            String status) {

        static Payment from(MonthlyBatchPaymentResult result) {
            var payment = result.payment();
            return new Payment(
                    payment.paymentId(), payment.claimId().value(), payment.beneficiaryId().value(),
                    result.beneficiaryName(), result.relationshipCode(), payment.benefitMonth(),
                    payment.grossAmount().toPlainString(), payment.offsetAmount().toPlainString(),
                    payment.netAmount().toPlainString(), payment.status());
        }
    }

    public record ExceptionResult(
            String claimId,
            String beneficiaryId,
            String reasonCode,
            String grossAmount,
            String netAmount) {

        static ExceptionResult from(MonthlyBatchException exception) {
            return new ExceptionResult(
                    exception.claimId().value(), exception.beneficiaryId().value(), exception.reasonCode(),
                    exception.expectedAmount().toPlainString(), exception.actualAmount().toPlainString());
        }
    }
}