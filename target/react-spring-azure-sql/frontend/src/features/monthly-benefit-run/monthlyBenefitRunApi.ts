import type { components } from "../../generated/api/schema";
import { SurvivorApiError, type Problem } from "../survivor-inquiry/survivorInquiryApi";

export type MonthlyBenefitRunRequest = components["schemas"]["MonthlyBenefitRunRequest"];
export type MonthlyBenefitRun = components["schemas"]["MonthlyBenefitRun"];

export interface MonthlyBenefitRunApi {
  run(request: MonthlyBenefitRunRequest): Promise<MonthlyBenefitRun>;
}

interface ApiOptions {
  getAccessToken: () => Promise<string>;
  baseUrl?: string;
  fetcher?: typeof fetch;
  timeoutMs?: number;
}

export function createMonthlyBenefitRunApi({
  getAccessToken,
  baseUrl = "/api/v1",
  fetcher = fetch,
  timeoutMs = 120_000,
}: ApiOptions): MonthlyBenefitRunApi {
  return {
    async run(request) {
      let token: string;
      try {
        token = await getAccessToken();
      } catch {
        throw new SurvivorApiError(401);
      }

      const response = await fetcher(`${baseUrl}/monthly-benefit-runs`, {
        method: "POST",
        headers: {
          Accept: "application/json, application/problem+json",
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
          "X-Correlation-ID": crypto.randomUUID(),
        },
        body: JSON.stringify(request),
        signal: AbortSignal.timeout(timeoutMs),
      });

      if (!response.ok) {
        throw new SurvivorApiError(response.status, await readProblem(response));
      }

      return (await response.json()) as MonthlyBenefitRun;
    },
  };
}

async function readProblem(response: Response): Promise<Problem | undefined> {
  if (!response.headers.get("content-type")?.includes("application/problem+json")) {
    return undefined;
  }
  return (await response.json()) as Problem;
}