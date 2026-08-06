package com.example.survdemo.application;

import java.util.List;

public record MonthlyBatchStagedResults(
        List<MonthlyBatchPaymentResult> payments,
        List<MonthlyBatchException> exceptions) {

    public MonthlyBatchStagedResults {
        payments = List.copyOf(payments);
        exceptions = List.copyOf(exceptions);
    }
}