import { FormEvent, useEffect, useRef, useState } from "react";
import { CalendarDays, RotateCcw, Search, ShieldCheck, UserRound } from "lucide-react";
import type { SurvivorEntitlement, SurvivorInquiryApi } from "./survivorInquiryApi";
import { SurvivorApiError } from "./survivorInquiryApi";

type ViewState =
  | { kind: "idle" }
  | { kind: "loading" }
  | { kind: "success"; entitlement: SurvivorEntitlement }
  | { kind: "error"; title: string; message: string; retryable: boolean };

interface Props {
  api: SurvivorInquiryApi;
}

export function SurvivorInquiryPage({ api }: Props) {
  const [claimId, setClaimId] = useState("");
  const [beneficiaryId, setBeneficiaryId] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [state, setState] = useState<ViewState>({ kind: "idle" });
  const claimInput = useRef<HTMLInputElement>(null);
  const statusRegion = useRef<HTMLDivElement>(null);

  useEffect(() => {
    claimInput.current?.focus();
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const errors = validate(claimId, beneficiaryId);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      setState({ kind: "error", title: "Check the inquiry details", message: "Correct the fields below and try again.", retryable: false });
      requestAnimationFrame(() => statusRegion.current?.focus());
      return;
    }

    setState({ kind: "loading" });
    try {
      const entitlement = await api.inquire(claimId.trim(), beneficiaryId.trim());
      setState({ kind: "success", entitlement });
    } catch (error) {
      setState(toErrorState(error));
    }
    requestAnimationFrame(() => statusRegion.current?.focus());
  }

  function clear() {
    setClaimId("");
    setBeneficiaryId("");
    setFieldErrors({});
    setState({ kind: "idle" });
    claimInput.current?.focus();
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-mark" aria-hidden="true">SD</div>
        <div>
          <p className="product-name">SURVDEMO</p>
          <p className="product-area">Benefits operations</p>
        </div>
        <div className="security-label"><ShieldCheck size={17} /> Protected inquiry</div>
      </header>

      <main>
        <section className="page-heading" aria-labelledby="page-title">
          <p className="eyebrow">Survivor services / Inquiry</p>
          <h1 id="page-title">Survivor entitlement</h1>
          <p>Find the current entitlement for a claim and beneficiary.</p>
        </section>

        <div className="workspace">
          <section className="query-panel" aria-labelledby="query-title">
            <div className="section-heading">
              <Search aria-hidden="true" />
              <div><h2 id="query-title">Inquiry details</h2><p>Both identifiers are required.</p></div>
            </div>
            <form onSubmit={submit} noValidate>
              <div className="field">
                <label htmlFor="claim-id">Claim ID</label>
                <input
                  id="claim-id"
                  ref={claimInput}
                  value={claimId}
                  onChange={(event) => setClaimId(event.target.value)}
                  maxLength={12}
                  autoComplete="off"
                  aria-describedby={`claim-hint${fieldErrors.claimId ? " claim-error" : ""}`}
                  aria-invalid={Boolean(fieldErrors.claimId)}
                />
                <span id="claim-hint" className="field-hint">Up to 12 characters</span>
                {fieldErrors.claimId && <span id="claim-error" className="field-error">{fieldErrors.claimId}</span>}
              </div>
              <div className="field">
                <label htmlFor="beneficiary-id">Beneficiary ID</label>
                <input
                  id="beneficiary-id"
                  value={beneficiaryId}
                  onChange={(event) => setBeneficiaryId(event.target.value)}
                  maxLength={10}
                  autoComplete="off"
                  aria-describedby={`beneficiary-hint${fieldErrors.beneficiaryId ? " beneficiary-error" : ""}`}
                  aria-invalid={Boolean(fieldErrors.beneficiaryId)}
                />
                <span id="beneficiary-hint" className="field-hint">Up to 10 characters</span>
                {fieldErrors.beneficiaryId && <span id="beneficiary-error" className="field-error">{fieldErrors.beneficiaryId}</span>}
              </div>
              <div className="actions">
                <button className="primary-action" type="submit" disabled={state.kind === "loading"}>
                  <Search size={18} /> {state.kind === "loading" ? "Searching..." : "Inquire"}
                </button>
                <button className="secondary-action" type="button" onClick={clear}>
                  <RotateCcw size={18} /> Clear
                </button>
              </div>
            </form>
          </section>

          <section className="result-panel" aria-labelledby="result-title">
            <div className="section-heading">
              <UserRound aria-hidden="true" />
              <div><h2 id="result-title">Entitlement result</h2><p>Read-only benefit information</p></div>
            </div>
            <div
              ref={statusRegion}
              className="result-content"
              role={state.kind === "error" ? "alert" : "status"}
              aria-live="polite"
              tabIndex={-1}
            >
              {state.kind === "idle" && <EmptyResult />}
              {state.kind === "loading" && <LoadingResult />}
              {state.kind === "success" && <EntitlementResult entitlement={state.entitlement} />}
              {state.kind === "error" && <ErrorResult state={state} />}
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}

function EmptyResult() {
  return <div className="empty-state"><Search size={30} aria-hidden="true" /><h3>No inquiry yet</h3><p>Enter the identifiers to retrieve an entitlement.</p></div>;
}

function LoadingResult() {
  return <div className="empty-state"><span className="spinner" aria-hidden="true" /><h3>Retrieving entitlement</h3><p>Please wait while the record is checked.</p></div>;
}

function EntitlementResult({ entitlement }: { entitlement: SurvivorEntitlement }) {
  return (
    <div className="entitlement">
      <div className="result-banner"><span>{entitlement.displayStatus.replaceAll("_", " ")}</span><strong>{entitlement.message}</strong></div>
      <dl>
        <div><dt>Beneficiary</dt><dd>{entitlement.beneficiaryName}</dd></div>
        <div><dt>Relationship</dt><dd>{entitlement.relationshipLabel} <small>({entitlement.relationshipCode})</small></dd></div>
        <div className="amount"><dt>Monthly amount</dt><dd><span aria-label={`${entitlement.monthlyAmount} dollars`}>${entitlement.monthlyAmount}</span></dd></div>
        <div><dt>Claim ID</dt><dd>{entitlement.claimId}</dd></div>
        <div><dt>Beneficiary ID</dt><dd>{entitlement.beneficiaryId}</dd></div>
        <div><dt><CalendarDays size={15} aria-hidden="true" /> Start date</dt><dd>{entitlement.startDate}</dd></div>
        <div><dt><CalendarDays size={15} aria-hidden="true" /> End date</dt><dd>{entitlement.endDate ?? "Not scheduled"}</dd></div>
      </dl>
    </div>
  );
}

function ErrorResult({ state }: { state: Extract<ViewState, { kind: "error" }> }) {
  return <div className="error-state"><p className="error-kicker">Inquiry not completed</p><h3>{state.title}</h3><p>{state.message}</p>{state.retryable && <p className="retry-note">Review the identifiers or try the inquiry again.</p>}</div>;
}

function validate(claimId: string, beneficiaryId: string) {
  const errors: Record<string, string> = {};
  if (!claimId.trim()) errors.claimId = "Enter a claim ID.";
  else if (claimId !== claimId.trim()) errors.claimId = "Remove leading or trailing spaces.";
  if (!beneficiaryId.trim()) errors.beneficiaryId = "Enter a beneficiary ID.";
  else if (beneficiaryId !== beneficiaryId.trim()) errors.beneficiaryId = "Remove leading or trailing spaces.";
  return errors;
}

function toErrorState(error: unknown): Extract<ViewState, { kind: "error" }> {
  if (error instanceof SurvivorApiError) {
    if (error.status === 401) return { kind: "error", title: "Sign-in required", message: "Your identity could not be verified for this inquiry.", retryable: false };
    if (error.status === 403) return { kind: "error", title: "Access denied", message: "You do not have permission to view survivor entitlements.", retryable: false };
    if (error.status === 404) return { kind: "error", title: "Entitlement not found", message: "No entitlement matches these identifiers.", retryable: true };
    if (error.status === 503) return { kind: "error", title: "Service unavailable", message: "The benefit service is temporarily unavailable.", retryable: true };
  }
  return { kind: "error", title: "Unexpected error", message: "The inquiry could not be completed.", retryable: true };
}