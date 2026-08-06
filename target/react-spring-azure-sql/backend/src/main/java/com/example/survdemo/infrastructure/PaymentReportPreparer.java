package com.example.survdemo.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class PaymentReportPreparer {

    private static final int CLAIM_START = 17;
    private static final int CLAIM_END = 29;
    private static final int BENEFICIARY_START = 29;
    private static final int BENEFICIARY_END = 39;

    public List<String> prepare(List<String> paymentRecords) {
        Objects.requireNonNull(paymentRecords, "paymentRecords must not be null");
        paymentRecords.forEach(this::requireFixedRecord);

        return paymentRecords.stream()
                .filter(record -> record.charAt(0) == 'D')
                .sorted(Comparator
                        .comparing((String record) -> record.substring(CLAIM_START, CLAIM_END))
                        .thenComparing(record -> record.substring(BENEFICIARY_START, BENEFICIARY_END)))
                .toList();
    }

    private void requireFixedRecord(String record) {
        Objects.requireNonNull(record, "payment record must not be null");
        if (record.length() != MonthlyBatchRecordFormatter.RECORD_LENGTH) {
            throw new IllegalArgumentException("Payment record must contain exactly 120 characters");
        }
    }
}