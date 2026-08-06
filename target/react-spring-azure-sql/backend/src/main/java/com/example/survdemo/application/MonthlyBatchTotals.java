package com.example.survdemo.application;

import java.math.BigDecimal;
import java.util.Objects;

public record MonthlyBatchTotals(int paymentCount, BigDecimal paymentTotal, int exceptionCount) {

    public MonthlyBatchTotals {
        Objects.requireNonNull(paymentTotal, "paymentTotal must not be null");
    }
}