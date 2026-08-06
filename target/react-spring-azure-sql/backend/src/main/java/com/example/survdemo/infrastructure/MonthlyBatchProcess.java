package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchOutcome;
import com.example.survdemo.application.MonthlyBenefitBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class MonthlyBatchProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyBatchProcess.class);
    private static final DateTimeFormatter CALCULATION_DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
    private static final int CONTROL_RECORD_LENGTH = 20;
    private static final int CALCULATION_DATE_LENGTH = 8;
    private static final int TECHNICAL_FAILURE_RETURN_CODE = 12;

    private final Function<MonthlyBatchCommand, MonthlyBatchOutcome> batchExecutor;
    private final Consumer<String> reportPreparer;

    public MonthlyBatchProcess(
            MonthlyBenefitBatchService batchService,
            Consumer<String> reportPreparer) {
        this(batchService::run, reportPreparer);
    }

    MonthlyBatchProcess(Function<MonthlyBatchCommand, MonthlyBatchOutcome> batchExecutor) {
        this(batchExecutor, runId -> { });
    }

    MonthlyBatchProcess(
            Function<MonthlyBatchCommand, MonthlyBatchOutcome> batchExecutor,
            Consumer<String> reportPreparer) {
        this.batchExecutor = Objects.requireNonNull(batchExecutor);
        this.reportPreparer = Objects.requireNonNull(reportPreparer);
    }

    public int run(String controlRecord) {
        MonthlyBatchCommand command;
        try {
            command = parse(controlRecord);
        } catch (IllegalArgumentException exception) {
            LOGGER.error("Monthly benefit batch control record is invalid: {}", exception.getMessage());
            return TECHNICAL_FAILURE_RETURN_CODE;
        }

        try {
            MonthlyBatchOutcome outcome = batchExecutor.apply(command);
            if (outcome.returnCode() < 8) {
                reportPreparer.accept(command.runId());
            }
            LOGGER.info(
                    "Monthly benefit batch completed: runId={}, returnCode={}, payments={}, paymentTotal={}, exceptions={}",
                    command.runId(),
                    outcome.returnCode(),
                    outcome.paymentCount(),
                    outcome.paymentTotal(),
                    outcome.exceptionCount());
            return outcome.returnCode();
        } catch (RuntimeException exception) {
            LOGGER.error("Monthly benefit batch launch failed: runId={}", command.runId(), exception);
            return TECHNICAL_FAILURE_RETURN_CODE;
        }
    }

    private MonthlyBatchCommand parse(String controlRecord) {
        if (controlRecord == null || controlRecord.length() != CONTROL_RECORD_LENGTH) {
            throw new IllegalArgumentException("Control record must contain exactly 20 characters");
        }

        String calculationDateValue = controlRecord.substring(0, CALCULATION_DATE_LENGTH);
        String runId = controlRecord.substring(CALCULATION_DATE_LENGTH);
        try {
            return new MonthlyBatchCommand(
                    runId,
                    LocalDate.parse(calculationDateValue, CALCULATION_DATE_FORMAT));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Control record date must use valid YYYYMMDD format", exception);
        }
    }
}