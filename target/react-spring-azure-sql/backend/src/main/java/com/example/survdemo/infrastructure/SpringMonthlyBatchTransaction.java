package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchTransaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SpringMonthlyBatchTransaction implements MonthlyBatchTransaction {

    private final TransactionTemplate transactionTemplate;

    public SpringMonthlyBatchTransaction(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
    }

    @Override
    public <T> T execute(TransactionalWork<T> work) {
        return transactionTemplate.execute(status -> work.execute());
    }
}