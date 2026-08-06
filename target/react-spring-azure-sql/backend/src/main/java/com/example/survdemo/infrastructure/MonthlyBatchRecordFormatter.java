package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchException;
import com.example.survdemo.application.MonthlyBatchPayment;
import com.example.survdemo.application.MonthlyBatchTotals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class MonthlyBatchRecordFormatter {

    public static final int RECORD_LENGTH = 120;

    private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Map<String, String> EXCEPTION_TEXT = Map.of(
            "B1", "BENEFICIARY IS NOT ACTIVE",
            "D1", "PAYMENT EXISTS FOR BENEFIT MONTH",
            "F1", "FAMILY MAXIMUM ALREADY REACHED",
            "Z1", "OFFSET REDUCES BENEFIT TO ZERO",
            "R1", "INVALID BENEFICIARY RELATIONSHIP",
            "P1", "INVALID BENEFIT PERCENTAGE");
    private static final String GENERIC_EXCEPTION_TEXT = "ENTITLEMENT VALIDATION FAILED";

    public String paymentHeader(MonthlyBatchCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        return requireRecordLength(
                "H"
                        + fixed(command.runId(), 12, "runId")
                        + command.calculationDate().format(COMPACT_DATE)
                        + command.benefitMonth().format(COMPACT_DATE)
                        + " ".repeat(91));
    }

    public String paymentDetail(
            MonthlyBatchPayment payment,
            String relationship,
            String beneficiaryName) {
        Objects.requireNonNull(payment, "payment must not be null");
        return requireRecordLength(
                "D"
                        + fixed(payment.paymentId(), 16, "paymentId")
                        + fixed(payment.claimId().value(), 12, "claimId")
                        + fixed(payment.beneficiaryId().value(), 10, "beneficiaryId")
                        + unsignedDecimal(payment.grossAmount(), 9, "grossAmount")
                        + unsignedDecimal(payment.offsetAmount(), 9, "offsetAmount")
                        + unsignedDecimal(payment.netAmount(), 9, "netAmount")
                        + fixed(relationship, 3, "relationship")
                        + truncated(beneficiaryName, 30, "beneficiaryName")
                        + fixed(payment.status(), 1, "status")
                        + " ".repeat(14));
    }

    public String paymentTrailer(MonthlyBatchTotals totals) {
        Objects.requireNonNull(totals, "totals must not be null");
        return requireRecordLength(
                "T"
                        + unsignedInteger(totals.paymentCount(), 9, "paymentCount")
                        + unsignedDecimal(totals.paymentTotal(), 11, "paymentTotal")
                        + unsignedInteger(totals.exceptionCount(), 9, "exceptionCount")
                        + " ".repeat(88));
    }

    public String exceptionDetail(MonthlyBatchException exception) {
        Objects.requireNonNull(exception, "exception must not be null");
        String reasonText = EXCEPTION_TEXT.getOrDefault(
                exception.reasonCode(), GENERIC_EXCEPTION_TEXT);
        return requireRecordLength(
                fixed(exception.claimId().value(), 12, "claimId")
                        + "|"
                        + fixed(exception.beneficiaryId().value(), 10, "beneficiaryId")
                        + "|"
                        + fixed(exception.reasonCode(), 2, "reasonCode")
                        + "|"
                        + signedEditedDecimal(exception.expectedAmount(), "expectedAmount")
                        + "|"
                        + signedEditedDecimal(exception.actualAmount(), "actualAmount")
                        + "|"
                        + fixed(reasonText, 40, "reasonText")
                        + " ".repeat(21));
    }

    private String unsignedDecimal(BigDecimal value, int integerDigits, String fieldName) {
        BigDecimal scaled = exactScale(value, fieldName);
        if (scaled.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        String digits = scaled.movePointRight(2).toBigIntegerExact().toString();
        return zeroPad(digits, integerDigits + 2, fieldName);
    }

    private String signedEditedDecimal(BigDecimal value, String fieldName) {
        BigDecimal scaled = exactScale(value, fieldName);
        String amount = String.format(Locale.ROOT, "%,.2f", scaled.abs());
        if (amount.length() > 14) {
            throw new IllegalArgumentException(fieldName + " exceeds legacy field width");
        }
        return (scaled.signum() < 0 ? "-" : " ") + " ".repeat(14 - amount.length()) + amount;
    }

    private BigDecimal exactScale(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(fieldName + " must have at most two decimal places", exception);
        }
    }

    private String unsignedInteger(long value, int width, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return zeroPad(Long.toString(value), width, fieldName);
    }

    private String zeroPad(String value, int width, String fieldName) {
        if (value.length() > width) {
            throw new IllegalArgumentException(fieldName + " exceeds legacy field width");
        }
        return "0".repeat(width - value.length()) + value;
    }

    private String fixed(String value, int width, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.length() > width) {
            throw new IllegalArgumentException(fieldName + " exceeds legacy field width");
        }
        return value + " ".repeat(width - value.length());
    }

    private String truncated(String value, int width, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String truncated = value.substring(0, Math.min(value.length(), width));
        return truncated + " ".repeat(width - truncated.length());
    }

    private String requireRecordLength(String record) {
        if (record.length() != RECORD_LENGTH) {
            throw new IllegalStateException("Expected 120-character record but formatted " + record.length());
        }
        return record;
    }
}