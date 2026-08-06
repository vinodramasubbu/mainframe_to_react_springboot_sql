package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchOutcome;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyBatchProcessTest {

    @Test
    void parsesTheFixedControlRecordAndReturnsTheServiceOutcome() {
        List<MonthlyBatchCommand> commands = new ArrayList<>();
        MonthlyBatchProcess process = new MonthlyBatchProcess(command -> {
            commands.add(command);
            return new MonthlyBatchOutcome(4, 2, new BigDecimal("1000.00"), 1);
        });

        int returnCode = process.run("20260831SRV202608001");

        assertThat(returnCode).isEqualTo(4);
        assertThat(commands).containsExactly(
                new MonthlyBatchCommand("SRV202608001", LocalDate.of(2026, 8, 31)));
    }

    @Test
    void returnsTechnicalFailureWithoutLaunchingForMalformedControlRecords() {
        List<MonthlyBatchCommand> commands = new ArrayList<>();
        MonthlyBatchProcess process = new MonthlyBatchProcess(command -> {
            commands.add(command);
            return new MonthlyBatchOutcome(0, 0, new BigDecimal("0.00"), 0);
        });

        assertThat(process.run("20260230SRV202602001")).isEqualTo(12);
        assertThat(process.run("20260831SHORT")).isEqualTo(12);
        assertThat(process.run("2026AB31SRV202608001")).isEqualTo(12);
        assertThat(commands).isEmpty();
    }

    @Test
    void preservesCleanAndTechnicalReturnCodes() {
        MonthlyBatchProcess clean = new MonthlyBatchProcess(command ->
                new MonthlyBatchOutcome(0, 1, new BigDecimal("500.00"), 0));
        MonthlyBatchProcess failed = new MonthlyBatchProcess(command ->
                new MonthlyBatchOutcome(12, 0, new BigDecimal("0.00"), 0));

        assertThat(clean.run("20260831SRV202608002")).isZero();
        assertThat(failed.run("20260831SRV202608003")).isEqualTo(12);
    }

    @Test
    void preparesThePaymentReportWhenTheCalculationReturnCodeIsBelowEight() {
        List<String> preparedRunIds = new ArrayList<>();
        MonthlyBatchProcess clean = new MonthlyBatchProcess(
                command -> new MonthlyBatchOutcome(0, 1, new BigDecimal("500.00"), 0),
                preparedRunIds::add);
        MonthlyBatchProcess withBusinessExceptions = new MonthlyBatchProcess(
                command -> new MonthlyBatchOutcome(4, 1, new BigDecimal("500.00"), 1),
                preparedRunIds::add);

        assertThat(clean.run("20260831SRV202608002")).isZero();
        assertThat(withBusinessExceptions.run("20260831SRV202608004")).isEqualTo(4);
        assertThat(preparedRunIds).containsExactly("SRV202608002", "SRV202608004");
    }

    @Test
    void suppressesThePaymentReportWhenTheCalculationReturnCodeIsEightOrHigher() {
        List<String> preparedRunIds = new ArrayList<>();
        MonthlyBatchProcess failed = new MonthlyBatchProcess(
                command -> new MonthlyBatchOutcome(12, 0, new BigDecimal("0.00"), 0),
                preparedRunIds::add);

        assertThat(failed.run("20260831SRV202608003")).isEqualTo(12);
        assertThat(preparedRunIds).isEmpty();
    }
}