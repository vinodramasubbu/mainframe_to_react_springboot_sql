package com.example.survdemo.application;

import com.example.survdemo.domain.ExceptionDecision;
import com.example.survdemo.domain.MonthlyBenefitCalculationEngine;
import com.example.survdemo.domain.MonthlyCalculationDecision;
import com.example.survdemo.domain.MonthlyCalculationInput;
import com.example.survdemo.domain.PaymentDecision;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class MonthlyBenefitBatchService {

    private static final int MAX_PAYMENT_SEQUENCE = 9_999;

    private final MonthlyBatchDataPort dataPort;
    private final MonthlyBatchRunLifecycle runLifecycle;
    private final MonthlyBatchTransaction transaction;
    private final MonthlyBatchOutputPort outputPort;
    private final MonthlyBenefitCalculationEngine calculationEngine;

    public MonthlyBenefitBatchService(
            MonthlyBatchDataPort dataPort,
            MonthlyBatchRunLifecycle runLifecycle,
            MonthlyBatchTransaction transaction,
            MonthlyBatchOutputPort outputPort) {
        this.dataPort = Objects.requireNonNull(dataPort);
        this.runLifecycle = Objects.requireNonNull(runLifecycle);
        this.transaction = Objects.requireNonNull(transaction);
        this.outputPort = Objects.requireNonNull(outputPort);
        this.calculationEngine = new MonthlyBenefitCalculationEngine();
    }

    public MonthlyBatchOutcome run(MonthlyBatchCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        try {
            runLifecycle.startRun(command);
        } catch (RuntimeException exception) {
            return MonthlyBatchOutcome.technicalFailure();
        }

        try {
            return transaction.execute(() -> executeCalculation(command));
        } catch (RuntimeException exception) {
            try {
                outputPort.discard(command.runId());
            } catch (RuntimeException discardException) {
                exception.addSuppressed(discardException);
            }
            try {
                runLifecycle.failRun(command.runId(), exception);
            } catch (RuntimeException failureStatusException) {
                exception.addSuppressed(failureStatusException);
            }
            return MonthlyBatchOutcome.technicalFailure();
        }
    }

    private MonthlyBatchOutcome executeCalculation(MonthlyBatchCommand command) {
        outputPort.open(command);
        List<MonthlyBatchEntitlement> entitlements = dataPort.findEligible(command.calculationDate()).stream()
                .sorted(Comparator
                        .comparing((MonthlyBatchEntitlement entitlement) ->
                                entitlement.calculationInput().claimId().value())
                        .thenComparing(entitlement -> entitlement.calculationInput().beneficiaryId().value()))
                .toList();
        List<MonthlyCalculationInput> calculationInputs = entitlements.stream()
                .map(entitlement -> withDuplicateFact(entitlement.calculationInput(), command))
                .toList();
        List<MonthlyCalculationDecision> decisions = calculationEngine.calculate(calculationInputs);

        int paymentCount = 0;
        int exceptionCount = 0;
        BigDecimal paymentTotal = new BigDecimal("0.00");

        for (int index = 0; index < decisions.size(); index++) {
            MonthlyCalculationDecision decision = decisions.get(index);
            MonthlyBatchEntitlement entitlement = entitlements.get(index);
            if (decision instanceof PaymentDecision payment) {
                paymentCount++;
                if (paymentCount > MAX_PAYMENT_SEQUENCE) {
                    throw new IllegalStateException("Payment sequence exceeds four digits");
                }
                persistPayment(command, entitlement, payment, paymentCount);
                paymentTotal = paymentTotal.add(payment.netAmount());
            } else if (decision instanceof ExceptionDecision exception) {
                persistException(command, exception);
                exceptionCount++;
            }
        }

        MonthlyBatchTotals totals = new MonthlyBatchTotals(paymentCount, paymentTotal, exceptionCount);
    outputPort.complete(command.runId(), totals);
        dataPort.completeRun(command.runId(), totals);
        return MonthlyBatchOutcome.completed(totals);
    }

    private MonthlyCalculationInput withDuplicateFact(
            MonthlyCalculationInput input,
            MonthlyBatchCommand command) {
        boolean duplicatePayment = dataPort.paymentExists(
                input.claimId(), input.beneficiaryId(), command.benefitMonth());
        return new MonthlyCalculationInput(
                input.claimId(),
                input.beneficiaryId(),
                input.validationInput(),
                input.familyMaximumPercentage(),
                duplicatePayment);
    }

    private void persistPayment(
            MonthlyBatchCommand command,
            MonthlyBatchEntitlement entitlement,
            PaymentDecision payment,
            int sequence) {
        String paymentId = command.runId() + "%04d".formatted(sequence);
        MonthlyBatchPayment batchPayment = new MonthlyBatchPayment(
                paymentId,
                command.runId(),
                payment.claimId(),
                payment.beneficiaryId(),
                command.benefitMonth(),
                payment.grossAmount(),
                entitlement.calculationInput().validationInput().otherIncomeOffset(),
                payment.netAmount(),
                payment.status());
            dataPort.savePayment(batchPayment);

        int rowsUpdated = dataPort.updateEntitlement(new MonthlyBatchEntitlementUpdate(
                payment.claimId(),
                payment.beneficiaryId(),
                payment.netAmount(),
                entitlement.versionNumber()));
        if (rowsUpdated != 1) {
            throw new OptimisticEntitlementUpdateException(
                    payment.claimId(), payment.beneficiaryId(), rowsUpdated);
        }
            outputPort.writePayment(
                batchPayment,
                entitlement.calculationInput().validationInput().relationship(),
                entitlement.beneficiaryName());
    }

    private void persistException(MonthlyBatchCommand command, ExceptionDecision exception) {
            MonthlyBatchException batchException = new MonthlyBatchException(
                command.runId(),
                exception.claimId(),
                exception.beneficiaryId(),
                exception.reasonCode(),
                exception.grossAmount(),
                exception.netAmount());
            outputPort.writeException(batchException);
            dataPort.saveException(batchException);
    }
}