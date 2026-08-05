package com.example.survdemo.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntitlementPresentationTest {

    @Test
    void claimStatusHasHighestPriority() {
        SurvivorEntitlementView view = EntitlementPresentation.present(entitlement("P", "I", "S", "SPS"));

        assertThat(view.displayStatus()).isEqualTo("NOT_APPROVED");
        assertThat(view.message()).isEqualTo("CLAIM IS NOT APPROVED");
    }

    @Test
    void beneficiaryStatusHasPriorityOverEntitlementStatus() {
        SurvivorEntitlementView view = EntitlementPresentation.present(entitlement("A", "I", "S", "CHD"));

        assertThat(view.displayStatus()).isEqualTo("INELIGIBLE");
        assertThat(view.message()).isEqualTo("BENEFICIARY IS NOT ELIGIBLE");
    }

    @Test
    void mapsEveryEntitlementStatusAndRelationship() {
        assertView("A", "SPS", "ACTIVE", "ENTITLEMENT FOUND", "SPOUSE");
        assertView("S", "CHD", "SUSPENDED", "ENTITLEMENT IS SUSPENDED", "CHILD");
        assertView("E", "DEP", "ENDED", "ENTITLEMENT HAS ENDED", "DEPENDENT");
        assertView("C", "UNK", "CANCELLED", "ENTITLEMENT IS CANCELLED", "UNKNOWN");
        assertView("X", "UNK", "UNKNOWN", "UNKNOWN ENTITLEMENT STATUS", "UNKNOWN");
    }

    @Test
    void preservesExactScaleTwoAmountAndNullableEndDate() {
        SurvivorEntitlementView view = EntitlementPresentation.present(entitlement("A", "A", "A", "SPS"));

        assertThat(view.monthlyAmount()).isEqualTo("1250.00");
        assertThat(view.endDate()).isNull();
    }

    @Test
    void rejectsBlankOversizedAndPaddedIdentifiers() {
        assertThatThrownBy(() -> new ClaimId(" "))
                .isInstanceOf(InvalidInquiryIdentifierException.class)
                .hasMessage("Claim ID is required");
        assertThatThrownBy(() -> new ClaimId("1234567890123"))
                .isInstanceOf(InvalidInquiryIdentifierException.class);
        assertThatThrownBy(() -> new BeneficiaryId(" BENE000001"))
                .isInstanceOf(InvalidInquiryIdentifierException.class)
                .hasMessage("Beneficiary ID cannot have leading or trailing spaces");
    }

    private void assertView(
            String entitlementStatus,
            String relationship,
            String expectedStatus,
            String expectedMessage,
            String expectedRelationship) {
        SurvivorEntitlementView view = EntitlementPresentation.present(
                entitlement("A", "A", entitlementStatus, relationship));
        assertThat(view.displayStatus()).isEqualTo(expectedStatus);
        assertThat(view.message()).isEqualTo(expectedMessage);
        assertThat(view.relationshipLabel()).isEqualTo(expectedRelationship);
    }

    private SurvivorEntitlement entitlement(
            String claimStatus,
            String beneficiaryStatus,
            String entitlementStatus,
            String relationship) {
        return new SurvivorEntitlement(
                new ClaimId("CLM000000001"),
                new BeneficiaryId("BENE000001"),
                "Jordan Morgan",
                relationship,
                beneficiaryStatus,
                claimStatus,
                entitlementStatus,
                new BigDecimal("1250.00"),
                LocalDate.of(2025, 1, 1),
                null);
    }
}