import { FormEvent, useRef, useState } from "react";
import { AlertTriangle, CalendarClock, CircleDollarSign, Play, ReceiptText, RotateCcw } from "lucide-react";
import { SurvivorApiError } from "../survivor-inquiry/survivorInquiryApi";
import type { MonthlyBenefitRun, MonthlyBenefitRunApi } from "./monthlyBenefitRunApi";

type ViewState =
  | { kind: "idle" }
  | { kind: "running" }
  | { kind: "success"; result: MonthlyBenefitRun }
  | { kind: "error"; title: string; message: string };

export function MonthlyBenefitRunPage({ api }: { api: MonthlyBenefitRunApi }) {
  const [calculationDate, setCalculationDate] = useState("");
  const [runId, setRunId] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [state, setState] = useState<ViewState>({ kind: "idle" });
  const statusRegion = useRef<HTMLDivElement>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const errors = validate(calculationDate, runId);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      setState({ kind: "error", title: "Check the run details", message: "Correct the fields and try again." });
      requestAnimationFrame(() => statusRegion.current?.focus());
      return;
    }

    setState({ kind: "running" });
    try {
      const result = await api.run({ calculationDate, runId: runId.toUpperCase() });
      setState({ kind: "success", result });
    } catch (error) {
      setState(toErrorState(error));
    }
    requestAnimationFrame(() => statusRegion.current?.focus());
  }

  function clear() {
    setCalculationDate("");
    setRunId("");
    setFieldErrors({});
    setState({ kind: "idle" });
  }

  return (
    <main>
      <section className="page-heading" aria-labelledby="monthly-run-title">
        <p className="eyebrow">Survivor services / Monthly processing</p>
        <h1 id="monthly-run-title">Monthly benefit run</h1>
        <p>Launch the survivor calculation and review its payment and exception results.</p>
      </section>

      <div className="workspace batch-workspace">
        <section className="query-panel" aria-labelledby="run-details-title">
          <div className="section-heading">
            <CalendarClock aria-hidden="true" />
            <div><h2 id="run-details-title">Run details</h2><p>Use a new run ID for every attempt.</p></div>
          </div>
          <form onSubmit={submit} noValidate>
            <div className="field">
              <label htmlFor="calculation-date">Calculation date</label>
              <input
                id="calculation-date"
                type="date"
                value={calculationDate}
                onChange={(event) => setCalculationDate(event.target.value)}
                aria-describedby={fieldErrors.calculationDate ? "calculation-date-error" : undefined}
                aria-invalid={Boolean(fieldErrors.calculationDate)}
              />
              {fieldErrors.calculationDate && <span id="calculation-date-error" className="field-error">{fieldErrors.calculationDate}</span>}
            </div>
            <div className="field">
              <label htmlFor="run-id">Run ID</label>
              <input
                id="run-id"
                value={runId}
                onChange={(event) => setRunId(event.target.value.toUpperCase())}
                maxLength={12}
                autoComplete="off"
                aria-describedby={`run-id-hint${fieldErrors.runId ? " run-id-error" : ""}`}
                aria-invalid={Boolean(fieldErrors.runId)}
              />
              <span id="run-id-hint" className="field-hint">Exactly 12 non-space characters</span>
              {fieldErrors.runId && <span id="run-id-error" className="field-error">{fieldErrors.runId}</span>}
            </div>
            <div className="actions">
              <button className="primary-action" type="submit" disabled={state.kind === "running"}>
                <Play size={18} /> {state.kind === "running" ? "Running..." : "Run monthly calculation"}
              </button>
              <button className="secondary-action" type="button" onClick={clear} disabled={state.kind === "running"}>
                <RotateCcw size={18} /> Clear
              </button>
            </div>
          </form>
        </section>

        <section className="result-panel batch-results" aria-labelledby="run-result-title">
          <div className="section-heading">
            <ReceiptText aria-hidden="true" />
            <div><h2 id="run-result-title">Run result</h2><p>Database and staged output outcome</p></div>
          </div>
          <div
            ref={statusRegion}
            className="result-content"
            role={state.kind === "error" ? "alert" : "status"}
            aria-live="polite"
            tabIndex={-1}
          >
            {state.kind === "idle" && <BatchEmptyState />}
            {state.kind === "running" && <BatchRunningState />}
            {state.kind === "error" && <BatchErrorState title={state.title} message={state.message} />}
            {state.kind === "success" && <BatchResult result={state.result} />}
          </div>
        </section>
      </div>
    </main>
  );
}

function BatchEmptyState() {
  return <div className="empty-state"><CalendarClock size={30} aria-hidden="true" /><h3>No run started</h3><p>Enter a calculation date and unique run ID.</p></div>;
}

function BatchRunningState() {
  return <div className="empty-state"><span className="spinner" aria-hidden="true" /><h3>Monthly calculation running</h3><p>Keep this page open until the result is returned.</p></div>;
}

function BatchErrorState({ title, message }: { title: string; message: string }) {
  return <div className="error-state"><p className="error-kicker">Run not started</p><h3>{title}</h3><p>{message}</p></div>;
}

function BatchResult({ result }: { result: MonthlyBenefitRun }) {
  const technicalFailure = result.returnCode === 12;
  const outcomeLabel = result.outcome === "CLEAN"
    ? "Completed cleanly"
    : result.outcome === "COMPLETED_WITH_EXCEPTIONS"
      ? "Completed with exceptions"
      : "Technical failure";

  return (
    <div className="batch-result">
      <div className={`run-banner ${technicalFailure ? "run-banner-failed" : ""}`}>
        <div><span>RC {result.returnCode}</span><strong>{outcomeLabel}</strong></div>
        <p>{result.runId} · {result.calculationDate}</p>
      </div>
      <dl className="run-metrics">
        <div><dt>Payments</dt><dd>{result.paymentCount}</dd></div>
        <div><dt>Payment total</dt><dd>${result.paymentTotal}</dd></div>
        <div><dt>Exceptions</dt><dd>{result.exceptionCount}</dd></div>
        <div><dt>Benefit month</dt><dd>{result.benefitMonth}</dd></div>
      </dl>

      {technicalFailure && (
        <div className="technical-failure"><AlertTriangle aria-hidden="true" /><p>The calculation failed. Verify the run record and use a new run ID before retrying.</p></div>
      )}

      <ResultTable title="Payments" icon={<CircleDollarSign aria-hidden="true" />} empty="No payments were created.">
        {result.payments.length > 0 && (
          <table><thead><tr><th>Beneficiary</th><th>Claim</th><th>Gross</th><th>Offset</th><th>Net</th></tr></thead>
            <tbody>{result.payments.map((payment) => <tr key={payment.paymentId}><td><strong>{payment.beneficiaryName}</strong><small>{payment.beneficiaryId} · {payment.relationshipCode}</small></td><td>{payment.claimId}</td><td>${payment.grossAmount}</td><td>${payment.offsetAmount}</td><td>${payment.netAmount}</td></tr>)}</tbody>
          </table>
        )}
      </ResultTable>

      <ResultTable title="Exceptions" icon={<AlertTriangle aria-hidden="true" />} empty="No business exceptions were recorded.">
        {result.exceptions.length > 0 && (
          <table><thead><tr><th>Reason</th><th>Claim</th><th>Beneficiary</th><th>Expected</th><th>Actual</th></tr></thead>
            <tbody>{result.exceptions.map((exception, index) => <tr key={`${exception.claimId}-${exception.beneficiaryId}-${index}`}><td><strong>{exception.reasonCode}</strong></td><td>{exception.claimId}</td><td>{exception.beneficiaryId}</td><td>${exception.grossAmount}</td><td>${exception.netAmount}</td></tr>)}</tbody>
          </table>
        )}
      </ResultTable>
    </div>
  );
}

function ResultTable({ title, icon, empty, children }: { title: string; icon: React.ReactNode; empty: string; children: React.ReactNode }) {
  return <section className="result-table-section"><h3>{icon}{title}</h3>{children || <p className="table-empty">{empty}</p>}</section>;
}

function validate(calculationDate: string, runId: string) {
  const errors: Record<string, string> = {};
  if (!calculationDate) errors.calculationDate = "Select a calculation date.";
  if (!/^\S{12}$/.test(runId)) errors.runId = "Run ID must contain exactly 12 characters with no spaces.";
  return errors;
}

function toErrorState(error: unknown): Extract<ViewState, { kind: "error" }> {
  if (error instanceof SurvivorApiError) {
    if (error.status === 401) return { kind: "error", title: "Sign-in required", message: "Your identity could not be verified." };
    if (error.status === 403) return { kind: "error", title: "Access denied", message: "You do not have permission to run monthly benefits." };
    if (error.status === 400) return { kind: "error", title: "Invalid run details", message: error.problem?.detail ?? "Check the run details." };
    if (error.status === 503) return { kind: "error", title: "Service unavailable", message: "The monthly benefit service is temporarily unavailable." };
  }
  return { kind: "error", title: "Run could not be completed", message: "An unexpected error prevented the monthly calculation." };
}