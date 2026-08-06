package com.example.survdemo.application;

public interface MonthlyBatchRunLifecycle {

    void startRun(MonthlyBatchCommand command);

    void failRun(String runId, RuntimeException failure);
}