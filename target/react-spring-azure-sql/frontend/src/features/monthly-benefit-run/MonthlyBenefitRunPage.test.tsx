import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import { MonthlyBenefitRunPage } from "./MonthlyBenefitRunPage";
import type { MonthlyBenefitRunApi } from "./monthlyBenefitRunApi";

const result = {
  runId: "SRV202608001",
  calculationDate: "2026-08-31",
  benefitMonth: "2026-08-01",
  returnCode: 4 as const,
  outcome: "COMPLETED_WITH_EXCEPTIONS" as const,
  paymentCount: 1,
  paymentTotal: "574.50",
  exceptionCount: 1,
  payments: [{
    paymentId: "SRV2026080010001",
    claimId: "CLM000000001",
    beneficiaryId: "BENE000001",
    beneficiaryName: "Jane Doe",
    relationshipCode: "SPS",
    benefitMonth: "2026-08-01",
    grossAmount: "600.00",
    offsetAmount: "25.50",
    netAmount: "574.50",
    status: "R",
  }],
  exceptions: [{
    claimId: "CLM000000002",
    beneficiaryId: "BENE000002",
    reasonCode: "F1",
    grossAmount: "500.00",
    netAmount: "0.00",
  }],
};

test("runs the monthly calculation and displays summary and result rows", async () => {
  const user = userEvent.setup();
  const run = vi.fn().mockResolvedValue(result);
  const api: MonthlyBenefitRunApi = { run };
  render(<MonthlyBenefitRunPage api={api} />);

  await user.type(screen.getByLabelText("Calculation date"), "2026-08-31");
  await user.type(screen.getByLabelText("Run ID"), "SRV202608001");
  await user.click(screen.getByRole("button", { name: "Run monthly calculation" }));

  expect(await screen.findByText("Completed with exceptions")).toBeVisible();
  expect(screen.getAllByText("$574.50")).toHaveLength(2);
  expect(screen.getByText("Jane Doe")).toBeVisible();
  expect(screen.getByText("F1")).toBeVisible();
  expect(run).toHaveBeenCalledWith({ runId: "SRV202608001", calculationDate: "2026-08-31" });
});

test("rejects an invalid run identifier without calling the API", async () => {
  const user = userEvent.setup();
  const run = vi.fn();
  render(<MonthlyBenefitRunPage api={{ run }} />);

  await user.type(screen.getByLabelText("Calculation date"), "2026-08-31");
  await user.type(screen.getByLabelText("Run ID"), "SHORT");
  await user.click(screen.getByRole("button", { name: "Run monthly calculation" }));

  expect(await screen.findByRole("alert")).toHaveFocus();
  expect(screen.getByLabelText("Run ID")).toHaveAccessibleDescription(/exactly 12 characters/i);
  expect(run).not.toHaveBeenCalled();
});