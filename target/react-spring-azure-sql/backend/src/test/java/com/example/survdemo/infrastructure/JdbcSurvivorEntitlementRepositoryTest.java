package com.example.survdemo.infrastructure;

import com.example.survdemo.api.CorrelationIdFilter;
import com.example.survdemo.api.SecurityProblemWriter;
import com.example.survdemo.application.SurvivorEntitlementRepository;
import com.example.survdemo.config.SecurityConfig;
import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import com.example.survdemo.domain.SurvivorEntitlement;
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

import java.math.BigDecimal;

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
class JdbcSurvivorEntitlementRepositoryTest {

    @Container
    @ServiceConnection
    static final MSSQLServerContainer<?> SQL_SERVER = new MSSQLServerContainer<>(
            DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-CU18-ubuntu-22.04"))
            .acceptLicense();

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private SurvivorEntitlementRepository repository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void seedInquiryData() {
        jdbcClient.sql("DELETE FROM SURVDEMO.SURVIVOR_ENTITLEMENT").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.BENEFICIARY").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.SURVIVOR_CLAIM").update();
        jdbcClient.sql("DELETE FROM SURVDEMO.POLICY").update();

        jdbcClient.sql("""
                INSERT INTO SURVDEMO.POLICY
                    (POLICY_ID, INSURED_ID, PRODUCT_CODE, BASE_MONTHLY_BENEFIT,
                     FAMILY_MAX_PCT, STATUS, EFFECTIVE_DATE)
                VALUES ('POL000000001', 'INS0000001', 'SURV01', 2500.00, 100.00, 'A', '2024-01-01')
                """).update();
        jdbcClient.sql("""
                INSERT INTO SURVDEMO.SURVIVOR_CLAIM
                    (CLAIM_ID, POLICY_ID, DATE_OF_DEATH, CLAIM_STATUS, APPROVED_DATE)
                VALUES ('CLM000000001', 'POL000000001', '2024-12-01', 'A', '2024-12-15')
                """).update();
        jdbcClient.sql("""
                INSERT INTO SURVDEMO.BENEFICIARY
                    (BENEFICIARY_ID, BENEFICIARY_NAME, DATE_OF_BIRTH, RELATIONSHIP, STATUS)
                VALUES ('BENE000001', N'Jordan Morgan', '1980-06-15', 'SPS', 'A')
                """).update();
        jdbcClient.sql("""
                INSERT INTO SURVDEMO.SURVIVOR_ENTITLEMENT
                    (CLAIM_ID, BENEFICIARY_ID, BENEFIT_PCT, START_DATE, STATUS, MONTHLY_AMOUNT)
                VALUES ('CLM000000001', 'BENE000001', 50.00, '2025-01-01', 'A', 1250.00)
                """).update();
    }

    @Test
    void appliesFlywayMigrationAndMapsExactInquiryValues() {
        SurvivorEntitlement entitlement = repository.find(
                        new ClaimId("CLM000000001"), new BeneficiaryId("BENE000001"))
                .orElseThrow();

        assertThat(entitlement.beneficiaryName()).isEqualTo("Jordan Morgan");
        assertThat(entitlement.relationshipCode()).isEqualTo("SPS");
        assertThat(entitlement.monthlyAmount()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(entitlement.endDate()).isNull();
    }
}