package com.example.survdemo.infrastructure;

import com.example.survdemo.api.CorrelationIdFilter;
import com.example.survdemo.api.SecurityProblemWriter;
import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchOutcome;
import com.example.survdemo.application.MonthlyBenefitBatchService;
import com.example.survdemo.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example",
                "survdemo.security.allowed-origin=https://localhost:5173"
        },
        classes = {
                com.example.survdemo.SurvdemoApplication.class,
                SecurityConfig.class,
                SecurityProblemWriter.class,
                CorrelationIdFilter.class
        })
@Testcontainers(disabledWithoutDocker = true)
class JdbcMonthlyBenefitBatchIntegrationTest {

    @Container
    @ServiceConnection
    static final MSSQLServerContainer<?> SQL_SERVER = new MSSQLServerContainer<>(
            DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-CU18-ubuntu-22.04"))
            .acceptLicense();

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private MonthlyBenefitBatchService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void seedBatchData() {
        jdbcClient.sql("DELETE FROM SURVDEMO.CALC_EXCEPTION").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.BENEFIT_PAYMENT").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.CALC_RUN").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.SURVIVOR_ENTITLEMENT").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.BENEFICIARY").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.SURVIVOR_CLAIM").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.POLICY").update();

        jdbcClient.sql("""
                INSERT INTO SURVDEMO.POLICY
                    (POLICY_ID, INSURED_ID, PRODUCT_CODE, BASE_MONTHLY_BENEFIT,
                     FAMILY_MAX_PCT, STATUS, EFFECTIVE_DATE)
                VALUES ('POL000000001', 'INS0000001', 'SURV01', 1000.00, 100.00, 'A', '2024-01-01')
                """).update();
        jdbcClient.sql("""
                INSERT INTO SURVDEMO.SURVIVOR_CLAIM
                    (CLAIM_ID, POLICY_ID, DATE_OF_DEATH, CLAIM_STATUS, APPROVED_DATE)
                VALUES ('CLM000000001', 'POL000000001', '2024-12-01', 'A', '2024-12-15')
                """).update();
        for (int index = 1; index <= 3; index++) {
            String beneficiaryId = "BENE%06d".formatted(index);
            jdbcClient.sql("""
                    INSERT INTO SURVDEMO.BENEFICIARY
                        (BENEFICIARY_ID, BENEFICIARY_NAME, DATE_OF_BIRTH, RELATIONSHIP, STATUS)
                    VALUES (:beneficiaryId, :beneficiaryName, '1980-06-15', 'SPS', 'A')
                    """)
                    .param("beneficiaryId", beneficiaryId)
                    .param("beneficiaryName", "Beneficiary " + index)
                    .update();
            jdbcClient.sql("""
                    INSERT INTO SURVDEMO.SURVIVOR_ENTITLEMENT
                        (CLAIM_ID, BENEFICIARY_ID, BENEFIT_PCT, START_DATE, STATUS)
                    VALUES ('CLM000000001', :beneficiaryId, 60.00, '2025-01-01', 'A')
                    """)
                    .param("beneficiaryId", beneficiaryId)
                    .update();
        }
    }

    @Test
    void commitsPaymentsExceptionEntitlementUpdatesAndCompletedRun() {
        MonthlyBatchOutcome outcome = service.run(new MonthlyBatchCommand(
                "RUN202607001", LocalDate.of(2026, 7, 31)));

        assertThat(outcome.returnCode()).isEqualTo(4);
        assertThat(outcome.paymentCount()).isEqualTo(2);
        assertThat(outcome.paymentTotal()).isEqualByComparingTo("1000.00");
        assertThat(outcome.exceptionCount()).isEqualTo(1);

        Map<String, Object> run = jdbcClient.sql("""
                SELECT RTRIM(STATUS) AS STATUS, PAYMENT_COUNT, PAYMENT_TOTAL, EXCEPTION_COUNT
                  FROM SURVDEMO.CALC_RUN
                 WHERE RUN_ID = 'RUN202607001'
                """).query().singleRow();
        assertThat(run).containsEntry("STATUS", "C")
                .containsEntry("PAYMENT_COUNT", 2)
                .containsEntry("EXCEPTION_COUNT", 1);
        assertThat(run.get("PAYMENT_TOTAL").toString()).isEqualTo("1000.00");

        assertThat(jdbcClient.sql("SELECT COUNT(*) FROM SURVDEMO.BENEFIT_PAYMENT")
                .query(Integer.class).single()).isEqualTo(2);
        assertThat(jdbcClient.sql("SELECT COUNT(*) FROM SURVDEMO.CALC_EXCEPTION")
                .query(Integer.class).single()).isEqualTo(1);
        assertThat(jdbcClient.sql("""
                SELECT COUNT(*) FROM SURVDEMO.SURVIVOR_ENTITLEMENT
                 WHERE VERSION_NO = 2 AND MONTHLY_AMOUNT > 0
                """).query(Integer.class).single()).isEqualTo(2);
    }
}