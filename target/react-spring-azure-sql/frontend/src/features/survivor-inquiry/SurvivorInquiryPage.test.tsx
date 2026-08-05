import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { axe } from "jest-axe";
import { expect, test, vi } from "vitest";
import { SurvivorInquiryPage } from "./SurvivorInquiryPage";
import { SurvivorApiError, type SurvivorInquiryApi } from "./survivorInquiryApi";

const entitlement = {
  claimId: "CLM000000001",
  beneficiaryId: "BENE000001",
  beneficiaryName: "Jordan Morgan",
  relationshipCode: "SPS",
  relationshipLabel: "SPOUSE" as const,
  monthlyAmount: "1250.00",
  startDate: "2025-01-01",
  endDate: null,
  displayStatus: "ACTIVE" as const,
  message: "ENTITLEMENT FOUND" as const,
};

function apiWith(result: SurvivorInquiryApi["inquire"]): SurvivorInquiryApi {
  return { inquire: result };
}

test("submits identifiers and displays the exact contract values", async () => {
  const user = userEvent.setup();
  const inquire = vi.fn().mockResolvedValue(entitlement);
  render(<SurvivorInquiryPage api={apiWith(inquire)} />);

  await user.type(screen.getByLabelText("Claim ID"), "CLM000000001");
  await user.type(screen.getByLabelText("Beneficiary ID"), "BENE000001");
  await user.click(screen.getByRole("button", { name: "Inquire" }));

  expect(await screen.findByText("Jordan Morgan")).toBeVisible();
  expect(screen.getByLabelText("1250.00 dollars")).toHaveTextContent("$1250.00");
  expect(screen.getByText("2025-01-01")).toBeVisible();
  expect(inquire).toHaveBeenCalledWith("CLM000000001", "BENE000001");
});

test("announces validation errors and associates them with fields", async () => {
  const user = userEvent.setup();
  render(<SurvivorInquiryPage api={apiWith(vi.fn())} />);

  await user.click(screen.getByRole("button", { name: "Inquire" }));

  expect(await screen.findByRole("alert")).toHaveFocus();
  expect(screen.getByLabelText("Claim ID")).toHaveAccessibleDescription(/Enter a claim ID/);
  expect(screen.getByLabelText("Beneficiary ID")).toHaveAttribute("aria-invalid", "true");
});

test.each([
  [401, "Sign-in required"],
  [403, "Access denied"],
  [404, "Entitlement not found"],
  [503, "Service unavailable"],
  [500, "Unexpected error"],
])("shows the safe state for an HTTP %s response", async (status, title) => {
  const user = userEvent.setup();
  const inquire = vi.fn().mockRejectedValue(new SurvivorApiError(status));
  render(<SurvivorInquiryPage api={apiWith(inquire)} />);

  await user.type(screen.getByLabelText("Claim ID"), "CLM000000001");
  await user.type(screen.getByLabelText("Beneficiary ID"), "BENE000001");
  await user.click(screen.getByRole("button", { name: "Inquire" }));

  expect(await screen.findByRole("heading", { name: title })).toBeVisible();
  expect(screen.getByRole("alert")).toHaveFocus();
});

test("clears results and restores focus to claim ID", async () => {
  const user = userEvent.setup();
  render(<SurvivorInquiryPage api={apiWith(vi.fn().mockResolvedValue(entitlement))} />);
  await user.type(screen.getByLabelText("Claim ID"), "CLM000000001");
  await user.type(screen.getByLabelText("Beneficiary ID"), "BENE000001");
  await user.click(screen.getByRole("button", { name: "Inquire" }));
  await screen.findByText("Jordan Morgan");

  await user.click(screen.getByRole("button", { name: "Clear" }));

  expect(screen.getByLabelText("Claim ID")).toHaveValue("");
  expect(screen.getByLabelText("Claim ID")).toHaveFocus();
  expect(screen.getByText("No inquiry yet")).toBeVisible();
});

test("has no detectable accessibility violations in the inquiry state", async () => {
  const { container } = render(<SurvivorInquiryPage api={apiWith(vi.fn())} />);
  await waitFor(() => expect(screen.getByLabelText("Claim ID")).toHaveFocus());
  expect(await axe(container)).toHaveNoViolations();
});