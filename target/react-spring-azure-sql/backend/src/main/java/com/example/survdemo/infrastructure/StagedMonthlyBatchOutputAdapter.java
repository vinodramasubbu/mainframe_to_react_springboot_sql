package com.example.survdemo.infrastructure;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchException;
import com.example.survdemo.application.MonthlyBatchOutputPort;
import com.example.survdemo.application.MonthlyBatchPayment;
import com.example.survdemo.application.MonthlyBatchPaymentResult;
import com.example.survdemo.application.MonthlyBatchResultPort;
import com.example.survdemo.application.MonthlyBatchStagedResults;
import com.example.survdemo.application.MonthlyBatchTotals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StagedMonthlyBatchOutputAdapter implements MonthlyBatchOutputPort, MonthlyBatchResultPort {

    private final MonthlyBatchRecordFormatter formatter;
    private final PaymentReportPreparer reportPreparer;
    private final Map<String, MutableStagedOutput> stagedOutputs = new HashMap<>();

    public StagedMonthlyBatchOutputAdapter(MonthlyBatchRecordFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter);
        this.reportPreparer = new PaymentReportPreparer();
    }

    @Override
    public synchronized void open(MonthlyBatchCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        MutableStagedOutput output = new MutableStagedOutput();
        output.paymentRecords.add(formatter.paymentHeader(command));
        stagedOutputs.put(command.runId(), output);
    }

    @Override
    public synchronized void writePayment(
            MonthlyBatchPayment payment,
            String relationship,
            String beneficiaryName) {
        MutableStagedOutput output = output(payment.runId());
        output.paymentRecords.add(formatter.paymentDetail(payment, relationship, beneficiaryName));
        output.payments.add(new MonthlyBatchPaymentResult(payment, relationship, beneficiaryName));
    }

    @Override
    public synchronized void writeException(MonthlyBatchException exception) {
        MutableStagedOutput output = output(exception.runId());
        output.exceptionRecords.add(formatter.exceptionDetail(exception));
        output.exceptions.add(exception);
    }

    @Override
    public synchronized void complete(String runId, MonthlyBatchTotals totals) {
        output(runId).paymentRecords.add(formatter.paymentTrailer(totals));
    }

    @Override
    public synchronized void discard(String runId) {
        stagedOutputs.remove(runId);
    }

    public synchronized StagedMonthlyBatchOutput stagedOutput(String runId) {
        MutableStagedOutput output = output(runId);
        return new StagedMonthlyBatchOutput(output.paymentRecords, output.exceptionRecords);
    }

    public synchronized void preparePaymentReport(String runId) {
        MutableStagedOutput output = output(runId);
        output.paymentReportRecords.clear();
        output.paymentReportRecords.addAll(reportPreparer.prepare(output.paymentRecords));
    }

    public synchronized List<String> paymentReport(String runId) {
        return List.copyOf(output(runId).paymentReportRecords);
    }

    @Override
    public synchronized MonthlyBatchStagedResults results(String runId) {
        MutableStagedOutput output = output(runId);
        return new MonthlyBatchStagedResults(output.payments, output.exceptions);
    }

    private MutableStagedOutput output(String runId) {
        MutableStagedOutput output = stagedOutputs.get(runId);
        if (output == null) {
            throw new IllegalStateException("No staged output for run " + runId);
        }
        return output;
    }

    public record StagedMonthlyBatchOutput(
            List<String> paymentRecords,
            List<String> exceptionRecords) {

        public StagedMonthlyBatchOutput {
            paymentRecords = List.copyOf(paymentRecords);
            exceptionRecords = List.copyOf(exceptionRecords);
        }
    }

    private static final class MutableStagedOutput {
        private final List<String> paymentRecords = new ArrayList<>();
        private final List<String> exceptionRecords = new ArrayList<>();
        private final List<String> paymentReportRecords = new ArrayList<>();
        private final List<MonthlyBatchPaymentResult> payments = new ArrayList<>();
        private final List<MonthlyBatchException> exceptions = new ArrayList<>();
    }
}