package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchDataPort;
import com.example.survdemo.application.MonthlyBatchEntitlement;
import com.example.survdemo.application.MonthlyBatchEntitlementUpdate;
import com.example.survdemo.application.MonthlyBatchException;
import com.example.survdemo.application.MonthlyBatchPayment;
import com.example.survdemo.application.MonthlyBatchTotals;
import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import com.example.survdemo.domain.MonthlyCalculationInput;
import com.example.survdemo.domain.ValidationInput;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class JdbcMonthlyBatchDataAdapter implements MonthlyBatchDataPort {

    private static final String ELIGIBLE_SQL = """
            SELECT RTRIM(C.CLAIM_ID) AS CLAIM_ID,
                   RTRIM(C.CLAIM_STATUS) AS CLAIM_STATUS,
                   RTRIM(B.BENEFICIARY_ID) AS BENEFICIARY_ID,
                   RTRIM(B.BENEFICIARY_NAME) AS BENEFICIARY_NAME,
                   RTRIM(B.RELATIONSHIP) AS RELATIONSHIP,
                   RTRIM(B.STATUS) AS BENEFICIARY_STATUS,
                   P.BASE_MONTHLY_BENEFIT,
                   P.FAMILY_MAX_PCT,
                   E.BENEFIT_PCT,
                   E.OTHER_INCOME_OFFSET,
                   E.VERSION_NO
              FROM SURVDEMO.POLICY P
              JOIN SURVDEMO.SURVIVOR_CLAIM C
                ON C.POLICY_ID = P.POLICY_ID
              JOIN SURVDEMO.SURVIVOR_ENTITLEMENT E WITH (UPDLOCK)
                ON E.CLAIM_ID = C.CLAIM_ID
              JOIN SURVDEMO.BENEFICIARY B
                ON B.BENEFICIARY_ID = E.BENEFICIARY_ID
             WHERE P.STATUS = 'A'
               AND C.CLAIM_STATUS = 'A'
               AND E.STATUS = 'A'
               AND E.START_DATE <= :calculationDate
               AND (E.END_DATE IS NULL OR E.END_DATE >= :calculationDate)
             ORDER BY C.CLAIM_ID, B.BENEFICIARY_ID
            """;

    private final JdbcClient jdbcClient;

    public JdbcMonthlyBatchDataAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<MonthlyBatchEntitlement> findEligible(LocalDate calculationDate) {
        return jdbcClient.sql(ELIGIBLE_SQL)
                .param("calculationDate", calculationDate)
                .query((resultSet, rowNumber) -> new MonthlyBatchEntitlement(
                        new MonthlyCalculationInput(
                                new ClaimId(resultSet.getString("CLAIM_ID")),
                                new BeneficiaryId(resultSet.getString("BENEFICIARY_ID")),
                                new ValidationInput(
                                        resultSet.getString("CLAIM_STATUS"),
                                        resultSet.getString("BENEFICIARY_STATUS"),
                                        resultSet.getString("RELATIONSHIP"),
                                        resultSet.getBigDecimal("BENEFIT_PCT"),
                                        resultSet.getBigDecimal("OTHER_INCOME_OFFSET"),
                                        resultSet.getBigDecimal("BASE_MONTHLY_BENEFIT")),
                                resultSet.getBigDecimal("FAMILY_MAX_PCT"),
                                false),
                              resultSet.getString("BENEFICIARY_NAME"),
                        resultSet.getInt("VERSION_NO")))
                .list();
    }

    @Override
    public boolean paymentExists(ClaimId claimId, BeneficiaryId beneficiaryId, LocalDate benefitMonth) {
        Integer count = jdbcClient.sql("""
                        SELECT COUNT(*)
                          FROM SURVDEMO.BENEFIT_PAYMENT
                         WHERE CLAIM_ID = :claimId
                           AND BENEFICIARY_ID = :beneficiaryId
                           AND BENEFIT_MONTH = :benefitMonth
                        """)
                .param("claimId", claimId.value())
                .param("beneficiaryId", beneficiaryId.value())
                .param("benefitMonth", benefitMonth)
                .query(Integer.class)
                .single();
        return count > 0;
    }

    @Override
    public void savePayment(MonthlyBatchPayment payment) {
        jdbcClient.sql("""
                        INSERT INTO SURVDEMO.BENEFIT_PAYMENT
                            (PAYMENT_ID, RUN_ID, CLAIM_ID, BENEFICIARY_ID, BENEFIT_MONTH,
                             GROSS_AMOUNT, OFFSET_AMOUNT, NET_AMOUNT, STATUS)
                        VALUES
                            (:paymentId, :runId, :claimId, :beneficiaryId, :benefitMonth,
                             :grossAmount, :offsetAmount, :netAmount, :status)
                        """)
                .param("paymentId", payment.paymentId())
                .param("runId", payment.runId())
                .param("claimId", payment.claimId().value())
                .param("beneficiaryId", payment.beneficiaryId().value())
                .param("benefitMonth", payment.benefitMonth())
                .param("grossAmount", payment.grossAmount())
                .param("offsetAmount", payment.offsetAmount())
                .param("netAmount", payment.netAmount())
                .param("status", payment.status())
                .update();
    }

    @Override
    public int updateEntitlement(MonthlyBatchEntitlementUpdate update) {
        return jdbcClient.sql("""
                        UPDATE SURVDEMO.SURVIVOR_ENTITLEMENT
                           SET MONTHLY_AMOUNT = :netAmount,
                               TOTAL_PAID = TOTAL_PAID + :netAmount,
                               VERSION_NO = VERSION_NO + 1,
                               UPDATED_TS = SYSUTCDATETIME()
                         WHERE CLAIM_ID = :claimId
                           AND BENEFICIARY_ID = :beneficiaryId
                           AND STATUS = 'A'
                           AND VERSION_NO = :expectedVersion
                        """)
                .param("netAmount", update.netAmount())
                .param("claimId", update.claimId().value())
                .param("beneficiaryId", update.beneficiaryId().value())
                .param("expectedVersion", update.expectedVersion())
                .update();
    }

    @Override
    public void saveException(MonthlyBatchException exception) {
        jdbcClient.sql("""
                        INSERT INTO SURVDEMO.CALC_EXCEPTION
                            (RUN_ID, CLAIM_ID, BENEFICIARY_ID, EXCEPTION_CODE,
                             EXPECTED_AMOUNT, ACTUAL_AMOUNT)
                        VALUES
                            (:runId, :claimId, :beneficiaryId, :reasonCode,
                             :expectedAmount, :actualAmount)
                        """)
                .param("runId", exception.runId())
                .param("claimId", exception.claimId().value())
                .param("beneficiaryId", exception.beneficiaryId().value())
                .param("reasonCode", exception.reasonCode())
                .param("expectedAmount", exception.expectedAmount())
                .param("actualAmount", exception.actualAmount())
                .update();
    }

    @Override
    public void completeRun(String runId, MonthlyBatchTotals totals) {
        int rowsUpdated = jdbcClient.sql("""
                        UPDATE SURVDEMO.CALC_RUN
                           SET PAYMENT_COUNT = :paymentCount,
                               PAYMENT_TOTAL = :paymentTotal,
                               EXCEPTION_COUNT = :exceptionCount,
                               STATUS = 'C',
                               COMPLETED_TS = SYSUTCDATETIME()
                         WHERE RUN_ID = :runId
                           AND STATUS = 'R'
                        """)
                .param("paymentCount", totals.paymentCount())
                .param("paymentTotal", totals.paymentTotal())
                .param("exceptionCount", totals.exceptionCount())
                .param("runId", runId)
                .update();
        if (rowsUpdated != 1) {
            throw new IllegalStateException("Expected one running calculation row to complete but updated "
                    + rowsUpdated);
        }
    }
}