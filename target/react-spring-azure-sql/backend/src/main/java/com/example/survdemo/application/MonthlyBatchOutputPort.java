package com.example.survdemo.application;

public interface MonthlyBatchOutputPort {

    void open(MonthlyBatchCommand command);

    void writePayment(MonthlyBatchPayment payment, String relationship, String beneficiaryName);

    void writeException(MonthlyBatchException exception);

    void complete(String runId, MonthlyBatchTotals totals);

    void discard(String runId);
}