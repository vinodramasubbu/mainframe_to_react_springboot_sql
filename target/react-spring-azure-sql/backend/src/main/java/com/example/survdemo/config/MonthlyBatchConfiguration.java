package com.example.survdemo.config;

import com.example.survdemo.application.MonthlyBatchDataPort;
import com.example.survdemo.application.MonthlyBatchOutputPort;
import com.example.survdemo.application.MonthlyBatchResultPort;
import com.example.survdemo.application.MonthlyBatchRunLifecycle;
import com.example.survdemo.application.MonthlyBatchTransaction;
import com.example.survdemo.application.MonthlyBenefitBatchService;
import com.example.survdemo.application.OnDemandMonthlyBatchService;
import com.example.survdemo.infrastructure.MonthlyBatchProcess;
import com.example.survdemo.infrastructure.MonthlyBatchRecordFormatter;
import com.example.survdemo.infrastructure.StagedMonthlyBatchOutputAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonthlyBatchConfiguration {

    @Bean
    MonthlyBenefitBatchService monthlyBenefitBatchService(
            MonthlyBatchDataPort dataPort,
            MonthlyBatchRunLifecycle runLifecycle,
            MonthlyBatchTransaction transaction,
            MonthlyBatchOutputPort outputPort) {
        return new MonthlyBenefitBatchService(dataPort, runLifecycle, transaction, outputPort);
    }

    @Bean
    StagedMonthlyBatchOutputAdapter monthlyBatchOutputPort() {
        return new StagedMonthlyBatchOutputAdapter(new MonthlyBatchRecordFormatter());
    }

    @Bean
    OnDemandMonthlyBatchService onDemandMonthlyBatchService(
            MonthlyBenefitBatchService batchService,
            MonthlyBatchResultPort resultPort) {
        return new OnDemandMonthlyBatchService(batchService, resultPort);
    }

    @Bean
    MonthlyBatchProcess monthlyBatchProcess(
            MonthlyBenefitBatchService batchService,
            StagedMonthlyBatchOutputAdapter outputAdapter) {
        return new MonthlyBatchProcess(batchService, outputAdapter::preparePaymentReport);
    }
}