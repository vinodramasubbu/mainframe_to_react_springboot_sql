package com.example.survdemo.application;

import java.util.List;
import java.util.Objects;

public record MonthlyBatchExecutionResult(
        MonthlyBatchCommand command,
        MonthlyBatchOutcome outcome,
        List<MonthlyBatchPaymentResult> payments,
        List<MonthlyBatchException> exceptions) {

    public MonthlyBatchExecutionResult {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        payments = List.copyOf(payments);
        exceptions = List.copyOf(exceptions);
    }
}