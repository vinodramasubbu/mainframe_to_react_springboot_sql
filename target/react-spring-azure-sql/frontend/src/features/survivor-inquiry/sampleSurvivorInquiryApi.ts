import {
  SurvivorApiError,
  type SurvivorEntitlement,
  type SurvivorInquiryApi,
} from "./survivorInquiryApi";

const samples: Readonly<Record<string, SurvivorEntitlement>> = {
  "CLM000000001:BENE000001": {
    claimId: "CLM000000001",
    beneficiaryId: "BENE000001",
    beneficiaryName: "Jordan Morgan",
    relationshipCode: "SPS",
    relationshipLabel: "SPOUSE",
    monthlyAmount: "1250.00",
    startDate: "2025-01-01",
    endDate: null,
    displayStatus: "ACTIVE",
    message: "ENTITLEMENT FOUND",
  },
  "CLM000000002:BENE000002": {
    claimId: "CLM000000002",
    beneficiaryId: "BENE000002",
    beneficiaryName: "Casey Rivera",
    relationshipCode: "SPS",
    relationshipLabel: "SPOUSE",
    monthlyAmount: "1180.00",
    startDate: "2024-11-01",
    endDate: null,
    displayStatus: "SUSPENDED",
    message: "ENTITLEMENT IS SUSPENDED",
  },
  "CLM000000003:BENE000003": {
    claimId: "CLM000000003",
    beneficiaryId: "BENE000003",
    beneficiaryName: "Taylor Chen",
    relationshipCode: "CHD",
    relationshipLabel: "CHILD",
    monthlyAmount: "450.00",
    startDate: "2023-09-01",
    endDate: "2025-06-30",
    displayStatus: "ENDED",
    message: "ENTITLEMENT HAS ENDED",
  },
  "CLM000000004:BENE000004": {
    claimId: "CLM000000004",
    beneficiaryId: "BENE000004",
    beneficiaryName: "Morgan Ellis",
    relationshipCode: "DEP",
    relationshipLabel: "DEPENDENT",
    monthlyAmount: "1000.00",
    startDate: "2024-03-01",
    endDate: "2024-12-31",
    displayStatus: "CANCELLED",
    message: "ENTITLEMENT IS CANCELLED",
  },
  "CLM000000005:BENE000005": {
    claimId: "CLM000000005",
    beneficiaryId: "BENE000005",
    beneficiaryName: "Riley Patel",
    relationshipCode: "SPS",
    relationshipLabel: "SPOUSE",
    monthlyAmount: "1375.00",
    startDate: "2025-02-01",
    endDate: null,
    displayStatus: "NOT_APPROVED",
    message: "CLAIM IS NOT APPROVED",
  },
  "CLM000000006:BENE000006": {
    claimId: "CLM000000006",
    beneficiaryId: "BENE000006",
    beneficiaryName: "Alex Johnson",
    relationshipCode: "CHD",
    relationshipLabel: "CHILD",
    monthlyAmount: "735.00",
    startDate: "2024-09-01",
    endDate: null,
    displayStatus: "INELIGIBLE",
    message: "BENEFICIARY IS NOT ELIGIBLE",
  },
};

export function createSampleSurvivorInquiryApi(): SurvivorInquiryApi {
  return {
    async inquire(claimId, beneficiaryId) {
      const sample = samples[`${claimId}:${beneficiaryId}`];
      if (sample) return sample;

      throw new SurvivorApiError(404, {
        type: "https://survdemo.example/problems/surv-entitlement-not-found",
        title: "Entitlement not found",
        status: 404,
        detail: "No synthetic entitlement exists for the supplied identifiers",
        code: "SURV-ENTITLEMENT-NOT-FOUND",
        correlationId: crypto.randomUUID(),
      });
    },
  };
}