import { useState } from "react";
import { CalendarClock, Search, ShieldCheck } from "lucide-react";
import { MonthlyBenefitRunPage } from "../features/monthly-benefit-run/MonthlyBenefitRunPage";
import { createMonthlyBenefitRunApi } from "../features/monthly-benefit-run/monthlyBenefitRunApi";
import { createSampleMonthlyBenefitRunApi } from "../features/monthly-benefit-run/sampleMonthlyBenefitRunApi";
import { SurvivorInquiryPage } from "../features/survivor-inquiry/SurvivorInquiryPage";
import { createSampleSurvivorInquiryApi } from "../features/survivor-inquiry/sampleSurvivorInquiryApi";
import { createSurvivorInquiryApi } from "../features/survivor-inquiry/survivorInquiryApi";

const getAccessToken = async () => { throw new Error("SURV-IDENTITY-NOT-CONFIGURED"); };
const securedInquiryApi = createSurvivorInquiryApi({ getAccessToken });
const securedBatchApi = createMonthlyBenefitRunApi({ getAccessToken });

export function App() {
  const [view, setView] = useState<"inquiry" | "monthly-run">("inquiry");
  const sampleDataEnabled = import.meta.env.DEV
    && new URLSearchParams(window.location.search).get("sampleData") === "true";
  const inquiryApi = sampleDataEnabled ? createSampleSurvivorInquiryApi() : securedInquiryApi;
  const batchApi = sampleDataEnabled ? createSampleMonthlyBenefitRunApi() : securedBatchApi;

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-mark" aria-hidden="true">SD</div>
        <div><p className="product-name">SURVDEMO</p><p className="product-area">Benefits operations</p></div>
        <div className="security-label"><ShieldCheck size={17} /> {sampleDataEnabled ? "Sample data" : "Protected operations"}</div>
      </header>
      <nav className="task-nav" aria-label="Benefits operations">
        <button className={view === "inquiry" ? "active" : ""} onClick={() => setView("inquiry")} aria-current={view === "inquiry" ? "page" : undefined}>
          <Search size={17} /> Entitlement inquiry
        </button>
        <button className={view === "monthly-run" ? "active" : ""} onClick={() => setView("monthly-run")} aria-current={view === "monthly-run" ? "page" : undefined}>
          <CalendarClock size={17} /> Monthly run
        </button>
      </nav>
      {view === "inquiry" ? <SurvivorInquiryPage api={inquiryApi} /> : <MonthlyBenefitRunPage api={batchApi} />}
    </div>
  );
}