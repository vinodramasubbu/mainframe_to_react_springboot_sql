package com.example.survdemo.application;

import java.time.LocalDate;
import java.util.Objects;

public record MonthlyBatchCommand(String runId, LocalDate calculationDate) {

    public MonthlyBatchCommand {
        Objects.requireNonNull(runId, "runId must not be null");
        Objects.requireNonNull(calculationDate, "calculationDate must not be null");
        if (runId.length() != 12) {
            throw new IllegalArgumentException("runId must contain exactly 12 characters");
        }
    }

    public LocalDate benefitMonth() {
        return calculationDate.withDayOfMonth(1);
    }
}