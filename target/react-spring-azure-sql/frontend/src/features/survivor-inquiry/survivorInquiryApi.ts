import type { components } from "../../generated/api/schema";

export type SurvivorEntitlement = components["schemas"]["SurvivorEntitlement"];
export type Problem = components["schemas"]["Problem"];

export interface SurvivorInquiryApi {
  inquire(claimId: string, beneficiaryId: string): Promise<SurvivorEntitlement>;
}

export class SurvivorApiError extends Error {
  constructor(
    readonly status: number,
    readonly problem?: Problem,
  ) {
    super(problem?.detail ?? problem?.title ?? "The inquiry could not be completed");
  }
}

interface ApiOptions {
  getAccessToken: () => Promise<string>;
  baseUrl?: string;
  fetcher?: typeof fetch;
  timeoutMs?: number;
}

export function createSurvivorInquiryApi({
  getAccessToken,
  baseUrl = "/api/v1",
  fetcher = fetch,
  timeoutMs = 10_000,
}: ApiOptions): SurvivorInquiryApi {
  return {
    async inquire(claimId, beneficiaryId) {
      let token: string;
      try {
        token = await getAccessToken();
      } catch {
        throw new SurvivorApiError(401, identityProblem());
      }

      const response = await fetcher(
        `${baseUrl}/survivor-entitlements/${encodeURIComponent(claimId)}/beneficiaries/${encodeURIComponent(beneficiaryId)}`,
        {
          headers: {
            Accept: "application/json, application/problem+json",
            Authorization: `Bearer ${token}`,
            "X-Correlation-ID": crypto.randomUUID(),
          },
          signal: AbortSignal.timeout(timeoutMs),
        },
      );

      if (!response.ok) {
        throw new SurvivorApiError(response.status, await readProblem(response));
      }

      return (await response.json()) as SurvivorEntitlement;
    },
  };
}

async function readProblem(response: Response): Promise<Problem | undefined> {
  if (!response.headers.get("content-type")?.includes("application/problem+json")) {
    return undefined;
  }
  return (await response.json()) as Problem;
}

function identityProblem(): Problem {
  return {
    type: "https://survdemo.example/problems/surv-authentication-required",
    title: "Authentication required",
    status: 401,
    detail: "Sign-in is not configured for this environment",
    code: "SURV-AUTHENTICATION-REQUIRED",
    correlationId: "not-issued",
  };
}