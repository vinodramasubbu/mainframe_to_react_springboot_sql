package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchException;
import com.example.survdemo.application.MonthlyBatchPayment;
import com.example.survdemo.application.MonthlyBatchTotals;
import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthlyBatchRecordFormatterTest {

    private final MonthlyBatchRecordFormatter formatter = new MonthlyBatchRecordFormatter();

    @Test
    void formatsExactPaymentHeaderDetailAndTrailerRecords() {
        MonthlyBatchCommand command = new MonthlyBatchCommand(
                "SRV202608001", LocalDate.of(2026, 8, 31));
        MonthlyBatchPayment payment = new MonthlyBatchPayment(
                "SRV2026080010001",
                command.runId(),
                new ClaimId("CLM000000001"),
                new BeneficiaryId("BENE000001"),
                command.benefitMonth(),
                new BigDecimal("600.00"),
                new BigDecimal("25.50"),
                new BigDecimal("574.50"),
                "R");

        String header = formatter.paymentHeader(command);
        String detail = formatter.paymentDetail(payment, "SPS", "JANE DOE");
        String trailer = formatter.paymentTrailer(
                new MonthlyBatchTotals(2, new BigDecimal("1000.00"), 1));

        assertThat(header).isEqualTo(
                "HSRV2026080012026083120260801" + " ".repeat(91));
        assertThat(detail).isEqualTo(
                "DSRV2026080010001CLM000000001BENE000001"
                        + "000000600000000000255000000057450SPS"
                        + "JANE DOE" + " ".repeat(22) + "R" + " ".repeat(14));
        assertThat(trailer).isEqualTo(
                "T0000000020000000100000000000001" + " ".repeat(88));
        assertThat(header).hasSize(120);
        assertThat(detail).hasSize(120);
        assertThat(trailer).hasSize(120);
    }

    @Test
    void formatsExactPipeDelimitedExceptionRecordAndReasonText() {
        MonthlyBatchException exception = new MonthlyBatchException(
                "SRV202608001",
                new ClaimId("CLM000000001"),
                new BeneficiaryId("BENE000001"),
                "Z1",
                new BigDecimal("25.50"),
                new BigDecimal("-5.25"));

        String record = formatter.exceptionDetail(exception);

        assertThat(record).isEqualTo(
                "CLM000000001|BENE000001|Z1|"
                        + "          25.50|-          5.25|"
                        + "OFFSET REDUCES BENEFIT TO ZERO" + " ".repeat(10)
                        + " ".repeat(21));
        assertThat(record).hasSize(120);
    }

    @Test
    void rejectsValuesThatCannotBeRepresentedWithoutChangingTheRecord() {
        MonthlyBatchPayment oversizedPayment = new MonthlyBatchPayment(
                "SRV2026080010001",
                "SRV202608001",
                new ClaimId("CLM000000001"),
                new BeneficiaryId("BENE000001"),
                LocalDate.of(2026, 8, 1),
                new BigDecimal("1000000000.00"),
                new BigDecimal("0.00"),
                new BigDecimal("1.00"),
                "R");

        assertThatThrownBy(() -> formatter.paymentDetail(oversizedPayment, "SPS", "JANE DOE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grossAmount");
        assertThatThrownBy(() -> formatter.paymentTrailer(
                new MonthlyBatchTotals(1, new BigDecimal("1.001"), 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentTotal");
        String longName = "A BENEFICIARY NAME THAT EXCEEDS THIRTY CHARACTERS";
        String detail = formatter.paymentDetail(
                new MonthlyBatchPayment(
                        "SRV2026080010001",
                        "SRV202608001",
                        new ClaimId("CLM000000001"),
                        new BeneficiaryId("BENE000001"),
                        LocalDate.of(2026, 8, 1),
                        new BigDecimal("1.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("1.00"),
                        "R"),
                "SPS",
                longName);
        assertThat(detail.substring(75, 105)).isEqualTo(longName.substring(0, 30));
    }
}