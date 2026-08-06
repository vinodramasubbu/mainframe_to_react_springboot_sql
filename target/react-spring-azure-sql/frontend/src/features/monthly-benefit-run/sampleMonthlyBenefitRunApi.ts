import type { MonthlyBenefitRunApi } from "./monthlyBenefitRunApi";

export function createSampleMonthlyBenefitRunApi(): MonthlyBenefitRunApi {
  return {
    async run(request) {
      const benefitMonth = `${request.calculationDate.slice(0, 7)}-01`;
      return {
        runId: request.runId,
        calculationDate: request.calculationDate,
        benefitMonth,
        returnCode: 4,
        outcome: "COMPLETED_WITH_EXCEPTIONS",
        paymentCount: 2,
        paymentTotal: "1024.50",
        exceptionCount: 1,
        payments: [
          {
            paymentId: `${request.runId}0001`,
            claimId: "CLM000000001",
            beneficiaryId: "BENE000001",
            beneficiaryName: "Jordan Morgan",
            relationshipCode: "SPS",
            benefitMonth,
            grossAmount: "600.00",
            offsetAmount: "25.50",
            netAmount: "574.50",
            status: "R",
          },
          {
            paymentId: `${request.runId}0002`,
            claimId: "CLM000000003",
            beneficiaryId: "BENE000003",
            beneficiaryName: "Taylor Chen",
            relationshipCode: "CHD",
            benefitMonth,
            grossAmount: "450.00",
            offsetAmount: "0.00",
            netAmount: "450.00",
            status: "R",
          },
        ],
        exceptions: [{
          claimId: "CLM000000002",
          beneficiaryId: "BENE000002",
          reasonCode: "F1",
          grossAmount: "500.00",
          netAmount: "0.00",
        }],
      };
    },
  };
}