import { expect, test } from "vitest";
import { createSampleSurvivorInquiryApi } from "./sampleSurvivorInquiryApi";

test("returns the exact active sample contract", async () => {
  const result = await createSampleSurvivorInquiryApi().inquire("CLM000000001", "BENE000001");

  expect(result).toMatchObject({
    beneficiaryName: "Jordan Morgan",
    monthlyAmount: "1250.00",
    displayStatus: "ACTIVE",
    message: "ENTITLEMENT FOUND",
  });
});

test("returns not found for an unknown sample pair", async () => {
  await expect(
    createSampleSurvivorInquiryApi().inquire("CLM999999999", "BENE999999"),
  ).rejects.toMatchObject({ status: 404 });
});