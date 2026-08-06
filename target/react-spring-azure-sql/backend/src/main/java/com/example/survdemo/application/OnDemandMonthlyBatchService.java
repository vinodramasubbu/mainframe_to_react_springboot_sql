package com.example.survdemo.application;

import java.util.List;
import java.util.Objects;

public final class OnDemandMonthlyBatchService {

    private static final int TECHNICAL_FAILURE_RETURN_CODE = 12;

    private final MonthlyBenefitBatchService batchService;
    private final MonthlyBatchResultPort resultPort;

    public OnDemandMonthlyBatchService(
            MonthlyBenefitBatchService batchService,
            MonthlyBatchResultPort resultPort) {
        this.batchService = Objects.requireNonNull(batchService);
        this.resultPort = Objects.requireNonNull(resultPort);
    }

    public MonthlyBatchExecutionResult run(MonthlyBatchCommand command) {
        MonthlyBatchOutcome outcome = batchService.run(command);
        if (outcome.returnCode() == TECHNICAL_FAILURE_RETURN_CODE) {
            return new MonthlyBatchExecutionResult(command, outcome, List.of(), List.of());
        }

        MonthlyBatchStagedResults results = resultPort.results(command.runId());
        return new MonthlyBatchExecutionResult(command, outcome, results.payments(), results.exceptions());
    }
}