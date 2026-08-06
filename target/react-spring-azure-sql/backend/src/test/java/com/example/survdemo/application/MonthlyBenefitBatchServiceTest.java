package com.example.survdemo.application;

import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import com.example.survdemo.domain.MonthlyCalculationInput;
import com.example.survdemo.domain.ValidationInput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyBenefitBatchServiceTest {

    private static final LocalDate CALCULATION_DATE = LocalDate.of(2026, 7, 31);

    @Test
    void persistsOrderedDecisionsAndCompletesWithBusinessExceptionSeverity() {
        FakeBatchGateway gateway = new FakeBatchGateway(List.of(
                entitlement("CLM000000001", "BENE000002", "60.00", 4),
                entitlement("CLM000000001", "BENE000001", "60.00", 7),
                entitlement("CLM000000001", "BENE000003", "60.00", 2)));
        MonthlyBenefitBatchService service = service(gateway);

        MonthlyBatchOutcome outcome = service.run(new MonthlyBatchCommand("RUN202607001", CALCULATION_DATE));

        assertThat(outcome.returnCode()).isEqualTo(4);
        assertThat(outcome.paymentCount()).isEqualTo(2);
        assertThat(outcome.paymentTotal()).isEqualByComparingTo("1000.00");
        assertThat(outcome.exceptionCount()).isEqualTo(1);
        assertThat(gateway.events).containsExactly(
                "start:RUN202607001:2026-07-01",
                "transaction:start",
            "output:open:RUN202607001:2026-07-31",
                "payment:RUN2026070010001:BENE000001:600.00:R",
                "entitlement:BENE000001:7:600.00",
            "output:payment:BENE000001:SPS:Beneficiary BENE000001",
                "payment:RUN2026070010002:BENE000002:400.00:C",
                "entitlement:BENE000002:4:400.00",
            "output:payment:BENE000002:SPS:Beneficiary BENE000002",
            "output:exception:BENE000003:F1",
                "exception:BENE000003:F1",
            "output:complete:2:1000.00:1",
                "complete:2:1000.00:1",
                "transaction:commit");
    }

    @Test
    void completesCleanRunWithZeroSeverity() {
        FakeBatchGateway gateway = new FakeBatchGateway(List.of(
                entitlement("CLM000000001", "BENE000001", "50.00", 1)));

        MonthlyBatchOutcome outcome = service(gateway)
                .run(new MonthlyBatchCommand("RUN202607002", CALCULATION_DATE));

        assertThat(outcome.returnCode()).isZero();
        assertThat(outcome.paymentCount()).isEqualTo(1);
        assertThat(outcome.exceptionCount()).isZero();
    }

    @Test
    void abortsCalculationAndMarksPrecommittedRunFailedOnOptimisticConflict() {
        FakeBatchGateway gateway = new FakeBatchGateway(List.of(
                entitlement("CLM000000001", "BENE000001", "50.00", 3),
                entitlement("CLM000000001", "BENE000002", "50.00", 5)));
        gateway.failEntitlementUpdate = true;

        MonthlyBatchOutcome outcome = service(gateway)
                .run(new MonthlyBatchCommand("RUN202607003", CALCULATION_DATE));

        assertThat(outcome.returnCode()).isEqualTo(12);
        assertThat(gateway.events).containsExactly(
                "start:RUN202607003:2026-07-01",
                "transaction:start",
                "output:open:RUN202607003:2026-07-31",
                "payment:RUN2026070030001:BENE000001:500.00:R",
                "entitlement:BENE000001:3:500.00",
                "transaction:rollback",
                "output:discard:RUN202607003",
                "failed:RUN202607003:OptimisticEntitlementUpdateException");
        assertThat(gateway.events).noneMatch(event -> event.startsWith("complete:"));
    }

    private MonthlyBenefitBatchService service(FakeBatchGateway gateway) {
        return new MonthlyBenefitBatchService(gateway, gateway, gateway, gateway);
    }

    private MonthlyBatchEntitlement entitlement(
            String claimId,
            String beneficiaryId,
            String benefitPercentage,
            int versionNumber) {
        return new MonthlyBatchEntitlement(
                new MonthlyCalculationInput(
                        new ClaimId(claimId),
                        new BeneficiaryId(beneficiaryId),
                        new ValidationInput(
                                "A",
                                "A",
                                "SPS",
                                new BigDecimal(benefitPercentage),
                                new BigDecimal("0.00"),
                                new BigDecimal("1000.00")),
                        new BigDecimal("100.00"),
                        false),
                    "Beneficiary " + beneficiaryId,
                versionNumber);
    }

    private static final class FakeBatchGateway
                    implements MonthlyBatchDataPort, MonthlyBatchRunLifecycle, MonthlyBatchTransaction,
                    MonthlyBatchOutputPort {

        private final List<MonthlyBatchEntitlement> entitlements;
        private final List<String> events = new ArrayList<>();
        private boolean failEntitlementUpdate;

        private FakeBatchGateway(List<MonthlyBatchEntitlement> entitlements) {
            this.entitlements = entitlements;
        }

        @Override
        public void startRun(MonthlyBatchCommand command) {
            events.add("start:" + command.runId() + ":" + command.benefitMonth());
        }

        @Override
        public void failRun(String runId, RuntimeException failure) {
            events.add("failed:" + runId + ":" + failure.getClass().getSimpleName());
        }

        @Override
        public void open(MonthlyBatchCommand command) {
            events.add("output:open:" + command.runId() + ":" + command.calculationDate());
        }

        @Override
        public void writePayment(
                MonthlyBatchPayment payment,
                String relationship,
                String beneficiaryName) {
            events.add("output:payment:" + payment.beneficiaryId().value() + ":"
                    + relationship + ":" + beneficiaryName);
        }

        @Override
        public void writeException(MonthlyBatchException exception) {
            events.add("output:exception:" + exception.beneficiaryId().value() + ":"
                    + exception.reasonCode());
        }

        @Override
        public void complete(String runId, MonthlyBatchTotals totals) {
            events.add("output:complete:" + totals.paymentCount() + ":" + totals.paymentTotal()
                    + ":" + totals.exceptionCount());
        }

        @Override
        public void discard(String runId) {
            events.add("output:discard:" + runId);
        }

        @Override
        public <T> T execute(MonthlyBatchTransaction.TransactionalWork<T> work) {
            events.add("transaction:start");
            try {
                T result = work.execute();
                events.add("transaction:commit");
                return result;
            } catch (RuntimeException exception) {
                events.add("transaction:rollback");
                throw exception;
            }
        }

        @Override
        public List<MonthlyBatchEntitlement> findEligible(LocalDate calculationDate) {
            return entitlements;
        }

        @Override
        public boolean paymentExists(ClaimId claimId, BeneficiaryId beneficiaryId, LocalDate benefitMonth) {
            return false;
        }

        @Override
        public void savePayment(MonthlyBatchPayment payment) {
            events.add("payment:" + payment.paymentId() + ":" + payment.beneficiaryId().value()
                    + ":" + payment.netAmount() + ":" + payment.status());
        }

        @Override
        public int updateEntitlement(MonthlyBatchEntitlementUpdate update) {
            events.add("entitlement:" + update.beneficiaryId().value() + ":" + update.expectedVersion()
                    + ":" + update.netAmount());
            return failEntitlementUpdate ? 0 : 1;
        }

        @Override
        public void saveException(MonthlyBatchException exception) {
            events.add("exception:" + exception.beneficiaryId().value() + ":" + exception.reasonCode());
        }

        @Override
        public void completeRun(String runId, MonthlyBatchTotals totals) {
            events.add("complete:" + totals.paymentCount() + ":" + totals.paymentTotal()
                    + ":" + totals.exceptionCount());
        }
    }
}