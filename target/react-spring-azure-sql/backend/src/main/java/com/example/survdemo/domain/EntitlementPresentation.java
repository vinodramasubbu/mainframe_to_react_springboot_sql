package com.example.survdemo.domain;

import java.math.RoundingMode;

public final class EntitlementPresentation {

    private EntitlementPresentation() {
    }

    public static SurvivorEntitlementView present(SurvivorEntitlement entitlement) {
        StatusMessage statusMessage = statusMessage(entitlement);
        return new SurvivorEntitlementView(
                entitlement.claimId().value(),
                entitlement.beneficiaryId().value(),
                entitlement.beneficiaryName(),
                entitlement.relationshipCode(),
                relationshipLabel(entitlement.relationshipCode()),
                entitlement.monthlyAmount().setScale(2, RoundingMode.UNNECESSARY).toPlainString(),
                entitlement.startDate(),
                entitlement.endDate(),
                statusMessage.status(),
                statusMessage.message());
    }

    private static String relationshipLabel(String relationshipCode) {
        return switch (relationshipCode) {
            case "SPS" -> "SPOUSE";
            case "CHD" -> "CHILD";
            case "DEP" -> "DEPENDENT";
            default -> "UNKNOWN";
        };
    }

    private static StatusMessage statusMessage(SurvivorEntitlement entitlement) {
        if (!"A".equals(entitlement.claimStatus())) {
            return new StatusMessage("NOT_APPROVED", "CLAIM IS NOT APPROVED");
        }
        if (!"A".equals(entitlement.beneficiaryStatus())) {
            return new StatusMessage("INELIGIBLE", "BENEFICIARY IS NOT ELIGIBLE");
        }
        return switch (entitlement.entitlementStatus()) {
            case "A" -> new StatusMessage("ACTIVE", "ENTITLEMENT FOUND");
            case "S" -> new StatusMessage("SUSPENDED", "ENTITLEMENT IS SUSPENDED");
            case "E" -> new StatusMessage("ENDED", "ENTITLEMENT HAS ENDED");
            case "C" -> new StatusMessage("CANCELLED", "ENTITLEMENT IS CANCELLED");
            default -> new StatusMessage("UNKNOWN", "UNKNOWN ENTITLEMENT STATUS");
        };
    }

    private record StatusMessage(String status, String message) {
    }
}