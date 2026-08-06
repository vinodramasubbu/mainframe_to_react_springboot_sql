package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchException;
import com.example.survdemo.application.MonthlyBatchPayment;
import com.example.survdemo.application.MonthlyBatchTotals;
import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StagedMonthlyBatchOutputAdapterTest {

    private final StagedMonthlyBatchOutputAdapter adapter =
            new StagedMonthlyBatchOutputAdapter(new MonthlyBatchRecordFormatter());

    @Test
    void stagesSeparateFixedPaymentAndExceptionRecordStreams() {
        MonthlyBatchCommand command = new MonthlyBatchCommand(
                "SRV202608001", LocalDate.of(2026, 8, 31));
        MonthlyBatchPayment payment = new MonthlyBatchPayment(
                "SRV2026080010001",
                command.runId(),
                new ClaimId("CLM000000001"),
                new BeneficiaryId("BENE000001"),
                command.benefitMonth(),
                new BigDecimal("600.00"),
                new BigDecimal("25.50"),
                new BigDecimal("574.50"),
                "R");
        MonthlyBatchException exception = new MonthlyBatchException(
                command.runId(),
                new ClaimId("CLM000000001"),
                new BeneficiaryId("BENE000002"),
                "F1",
                new BigDecimal("600.00"),
                new BigDecimal("400.00"));

        adapter.open(command);
        adapter.writePayment(payment, "SPS", "JANE DOE");
        adapter.writeException(exception);
        adapter.complete(command.runId(), new MonthlyBatchTotals(1, new BigDecimal("574.50"), 1));

        StagedMonthlyBatchOutputAdapter.StagedMonthlyBatchOutput output =
                adapter.stagedOutput(command.runId());
        assertThat(output.paymentRecords())
                .containsExactly(
                        "HSRV2026080012026083120260801" + " ".repeat(91),
                        "DSRV2026080010001CLM000000001BENE000001"
                                + "000000600000000000255000000057450SPS"
                                + "JANE DOE" + " ".repeat(22) + "R" + " ".repeat(14),
                        "T0000000010000000057450000000001" + " ".repeat(88))
                .allSatisfy(record -> assertThat(record).hasSize(120));
        assertThat(output.exceptionRecords())
                .singleElement()
                .satisfies(record -> assertThat(record).hasSize(120));
        assertThatThrownBy(() -> output.paymentRecords().add("record"))
                .isInstanceOf(UnsupportedOperationException.class);

        adapter.preparePaymentReport(command.runId());

        assertThat(adapter.paymentReport(command.runId()))
                .containsExactly(output.paymentRecords().get(1));
        assertThatThrownBy(() -> adapter.paymentReport(command.runId()).add("record"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void discardsAllStagedRecordsForAFailedRun() {
        MonthlyBatchCommand command = new MonthlyBatchCommand(
                "SRV202608002", LocalDate.of(2026, 8, 31));
        adapter.open(command);

        adapter.discard(command.runId());

        assertThatThrownBy(() -> adapter.stagedOutput(command.runId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(command.runId());
    }
}