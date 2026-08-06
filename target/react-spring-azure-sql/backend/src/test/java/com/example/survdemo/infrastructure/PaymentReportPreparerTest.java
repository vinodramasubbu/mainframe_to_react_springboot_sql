package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchPayment;
import com.example.survdemo.application.MonthlyBatchTotals;
import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentReportPreparerTest {

    private final MonthlyBatchRecordFormatter formatter = new MonthlyBatchRecordFormatter();
    private final PaymentReportPreparer preparer = new PaymentReportPreparer();

    @Test
    void retainsOnlyDetailsAndSortsByClaimThenBeneficiaryWithEqualKeysStable() {
        MonthlyBatchCommand command = new MonthlyBatchCommand(
                "SRV202608001", LocalDate.of(2026, 8, 31));
        String claimTwo = detail(command, "0001", "CLM000000002", "BENE000001");
        String equalKeyFirst = detail(command, "0002", "CLM000000001", "BENE000002");
        String beneficiaryOne = detail(command, "0003", "CLM000000001", "BENE000001");
        String equalKeySecond = detail(command, "0004", "CLM000000001", "BENE000002");

        List<String> report = preparer.prepare(List.of(
                formatter.paymentHeader(command),
                claimTwo,
                equalKeyFirst,
                beneficiaryOne,
                equalKeySecond,
                formatter.paymentTrailer(new MonthlyBatchTotals(4, new BigDecimal("400.00"), 0))));

        assertThat(report).containsExactly(
                beneficiaryOne,
                equalKeyFirst,
                equalKeySecond,
                claimTwo);
        assertThat(report).allSatisfy(record -> assertThat(record).hasSize(120));
        assertThatThrownBy(() -> report.add(claimTwo))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsInputThatIsNotACompleteFixedRecord() {
        assertThatThrownBy(() -> preparer.prepare(List.of("Dshort")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("120");
    }

    private String detail(
            MonthlyBatchCommand command,
            String sequence,
            String claimId,
            String beneficiaryId) {
        MonthlyBatchPayment payment = new MonthlyBatchPayment(
                command.runId() + sequence,
                command.runId(),
                new ClaimId(claimId),
                new BeneficiaryId(beneficiaryId),
                command.benefitMonth(),
                new BigDecimal("100.00"),
                new BigDecimal("0.00"),
                new BigDecimal("100.00"),
                "R");
        return formatter.paymentDetail(payment, "SPS", "BENEFICIARY");
    }
}