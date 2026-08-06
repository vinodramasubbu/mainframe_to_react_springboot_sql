package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchRunLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class JdbcMonthlyBatchRunLifecycle implements MonthlyBatchRunLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcMonthlyBatchRunLifecycle.class);

    private final JdbcClient jdbcClient;
    private final TransactionTemplate requiresNewTransaction;

    public JdbcMonthlyBatchRunLifecycle(
            JdbcClient jdbcClient,
            PlatformTransactionManager transactionManager) {
        this.jdbcClient = jdbcClient;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void startRun(MonthlyBatchCommand command) {
        try {
            requiresNewTransaction.executeWithoutResult(status -> jdbcClient.sql("""
                            INSERT INTO SURVDEMO.CALC_RUN (RUN_ID, CALC_DATE, BENEFIT_MONTH, STATUS)
                            VALUES (:runId, :calculationDate, :benefitMonth, 'R')
                            """)
                    .param("runId", command.runId())
                    .param("calculationDate", command.calculationDate())
                    .param("benefitMonth", command.benefitMonth())
                    .update());
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to start monthly benefit run {}", command.runId(), exception);
            throw exception;
        }
    }

    @Override
    public void failRun(String runId, RuntimeException failure) {
        LOGGER.error("Monthly benefit run {} failed", runId, failure);
        requiresNewTransaction.executeWithoutResult(status -> {
            int rowsUpdated = jdbcClient.sql("""
                            UPDATE SURVDEMO.CALC_RUN
                               SET STATUS = 'F', COMPLETED_TS = SYSUTCDATETIME()
                             WHERE RUN_ID = :runId
                               AND STATUS = 'R'
                            """)
                    .param("runId", runId)
                    .update();
            if (rowsUpdated != 1) {
                throw new IllegalStateException("Expected one running calculation row to fail but updated "
                        + rowsUpdated);
            }
        });
    }
}