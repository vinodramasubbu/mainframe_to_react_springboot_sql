import { expect, test } from "@playwright/test";

test("TASK-SURV-001 fails closed until the approved identity provider is integrated", async ({ page }) => {
  await page.goto("/");
  await page.getByLabel("Claim ID").fill("CLM000000001");
  await page.getByLabel("Beneficiary ID").fill("BENE000001");
  await page.getByRole("button", { name: "Inquire" }).click();

  const alert = page.getByRole("alert");
  await expect(alert).toContainText("Sign-in required");
  await expect(alert).toBeFocused();
});