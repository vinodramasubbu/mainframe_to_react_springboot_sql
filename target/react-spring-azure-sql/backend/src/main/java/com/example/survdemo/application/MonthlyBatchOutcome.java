package com.example.survdemo.application;

import java.math.BigDecimal;
import java.util.Objects;

public record MonthlyBatchOutcome(
        int returnCode,
        int paymentCount,
        BigDecimal paymentTotal,
        int exceptionCount) {

    public MonthlyBatchOutcome {
        Objects.requireNonNull(paymentTotal, "paymentTotal must not be null");
    }

    static MonthlyBatchOutcome completed(MonthlyBatchTotals totals) {
        int returnCode = totals.exceptionCount() == 0 ? 0 : 4;
        return new MonthlyBatchOutcome(
                returnCode, totals.paymentCount(), totals.paymentTotal(), totals.exceptionCount());
    }

    static MonthlyBatchOutcome technicalFailure() {
        return new MonthlyBatchOutcome(12, 0, new BigDecimal("0.00"), 0);
    }
}