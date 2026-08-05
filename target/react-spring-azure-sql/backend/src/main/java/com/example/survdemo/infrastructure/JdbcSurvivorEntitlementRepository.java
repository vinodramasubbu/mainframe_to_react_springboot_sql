package com.example.survdemo.infrastructure;

import com.example.survdemo.application.SurvivorEntitlementRepository;
import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import com.example.survdemo.domain.SurvivorEntitlement;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Optional;

@Repository
public class JdbcSurvivorEntitlementRepository implements SurvivorEntitlementRepository {

    private static final String INQUIRY_SQL = """
            SELECT RTRIM(C.CLAIM_ID) AS CLAIM_ID,
                   RTRIM(E.BENEFICIARY_ID) AS BENEFICIARY_ID,
                   B.BENEFICIARY_NAME,
                   RTRIM(B.RELATIONSHIP) AS RELATIONSHIP,
                   RTRIM(B.STATUS) AS BENEFICIARY_STATUS,
                   RTRIM(C.CLAIM_STATUS) AS CLAIM_STATUS,
                   RTRIM(E.STATUS) AS ENTITLEMENT_STATUS,
                   E.MONTHLY_AMOUNT,
                   E.START_DATE,
                   E.END_DATE
              FROM SURVDEMO.SURVIVOR_CLAIM C
              JOIN SURVDEMO.SURVIVOR_ENTITLEMENT E
                ON E.CLAIM_ID = C.CLAIM_ID
              JOIN SURVDEMO.BENEFICIARY B
                ON B.BENEFICIARY_ID = E.BENEFICIARY_ID
             WHERE C.CLAIM_ID = :claimId
               AND E.BENEFICIARY_ID = :beneficiaryId
            """;

    private final JdbcClient jdbcClient;

    public JdbcSurvivorEntitlementRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<SurvivorEntitlement> find(ClaimId claimId, BeneficiaryId beneficiaryId) {
        return jdbcClient.sql(INQUIRY_SQL)
                .param("claimId", claimId.value())
                .param("beneficiaryId", beneficiaryId.value())
                .query((resultSet, rowNumber) -> {
                    Date endDate = resultSet.getDate("END_DATE");
                    return new SurvivorEntitlement(
                            new ClaimId(resultSet.getString("CLAIM_ID")),
                            new BeneficiaryId(resultSet.getString("BENEFICIARY_ID")),
                            resultSet.getString("BENEFICIARY_NAME"),
                            resultSet.getString("RELATIONSHIP"),
                            resultSet.getString("BENEFICIARY_STATUS"),
                            resultSet.getString("CLAIM_STATUS"),
                            resultSet.getString("ENTITLEMENT_STATUS"),
                            resultSet.getBigDecimal("MONTHLY_AMOUNT"),
                            resultSet.getDate("START_DATE").toLocalDate(),
                            endDate == null ? null : endDate.toLocalDate());
                })
                .optional();
    }
}