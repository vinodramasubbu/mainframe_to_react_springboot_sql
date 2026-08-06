package com.example.survdemo;

import com.example.survdemo.infrastructure.MonthlyBatchProcess;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class SurvdemoApplication {

    private static final String BATCH_CONTROL_RECORD_ARGUMENT = "--survdemo.batch.control-record=";

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SurvdemoApplication.class);
        boolean batchLaunch = isBatchLaunch(args);
        if (batchLaunch) {
            application.setWebApplicationType(WebApplicationType.NONE);
        }

        ConfigurableApplicationContext context = application.run(args);
        if (batchLaunch) {
            String controlRecord = context.getEnvironment()
                    .getRequiredProperty("survdemo.batch.control-record");
            int returnCode = context.getBean(MonthlyBatchProcess.class).run(controlRecord);
            int exitCode = SpringApplication.exit(context, () -> returnCode);
            System.exit(exitCode);
        }
    }

    static boolean isBatchLaunch(String[] args) {
        return Arrays.stream(args).anyMatch(argument -> argument.startsWith(BATCH_CONTROL_RECORD_ARGUMENT));
    }
}