import { SurvivorInquiryPage } from "../features/survivor-inquiry/SurvivorInquiryPage";
import { createSampleSurvivorInquiryApi } from "../features/survivor-inquiry/sampleSurvivorInquiryApi";
import { createSurvivorInquiryApi } from "../features/survivor-inquiry/survivorInquiryApi";

const securedApi = createSurvivorInquiryApi({
  getAccessToken: async () => {
    throw new Error("SURV-IDENTITY-NOT-CONFIGURED");
  },
});

export function App() {
  const sampleDataEnabled = import.meta.env.DEV
    && new URLSearchParams(window.location.search).get("sampleData") === "true";
  const api = sampleDataEnabled ? createSampleSurvivorInquiryApi() : securedApi;

  return <SurvivorInquiryPage api={api} />;
}