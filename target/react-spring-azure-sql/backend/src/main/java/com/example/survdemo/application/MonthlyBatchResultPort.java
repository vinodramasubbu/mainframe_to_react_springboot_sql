package com.example.survdemo.application;

public interface MonthlyBatchResultPort {

    MonthlyBatchStagedResults results(String runId);
}