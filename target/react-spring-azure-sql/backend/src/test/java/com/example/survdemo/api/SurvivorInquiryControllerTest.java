package com.example.survdemo.api;

import com.example.survdemo.application.EntitlementNotFoundException;
import com.example.survdemo.application.SurvivorInquiryService;
import com.example.survdemo.config.SecurityConfig;
import com.example.survdemo.domain.SurvivorEntitlementView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = {
                SurvivorInquiryController.class,
                ApiExceptionHandler.class,
                CorrelationIdFilter.class,
                SecurityProblemWriter.class,
                SecurityConfig.class
        },
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example",
                "survdemo.security.allowed-origin=https://localhost:5173"
        })
class SurvivorInquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SurvivorInquiryService inquiryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(authorities = "SCOPE_survivor.inquiry")
    void returnsContractResponseForAuthorizedInquiry() throws Exception {
        when(inquiryService.inquire("CLM000000001", "BENE000001"))
                .thenReturn(new SurvivorEntitlementView(
                        "CLM000000001", "BENE000001", "Jordan Morgan", "SPS", "SPOUSE",
                        "1250.00", LocalDate.of(2025, 1, 1), null, "ACTIVE", "ENTITLEMENT FOUND"));

        mockMvc.perform(get("/api/v1/survivor-entitlements/CLM000000001/beneficiaries/BENE000001")
                        .header(CorrelationIdFilter.HEADER, "test-correlation"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.HEADER, "test-correlation"))
                .andExpect(jsonPath("$.monthlyAmount").value("1250.00"))
                .andExpect(jsonPath("$.displayStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("ENTITLEMENT FOUND"));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/survivor-entitlements/CLM000000001/beneficiaries/BENE000001"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SURV-AUTHENTICATION-REQUIRED"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_other")
    void requiresInquiryScope() throws Exception {
        mockMvc.perform(get("/api/v1/survivor-entitlements/CLM000000001/beneficiaries/BENE000001"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SURV-FORBIDDEN"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_survivor.inquiry")
    void mapsNotFoundToLegacyMessage() throws Exception {
        when(inquiryService.inquire("CLM000000001", "BENE000001"))
                .thenThrow(new EntitlementNotFoundException());

        mockMvc.perform(get("/api/v1/survivor-entitlements/CLM000000001/beneficiaries/BENE000001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SURV-ENTITLEMENT-NOT-FOUND"))
                .andExpect(jsonPath("$.detail").value("ENTITLEMENT NOT FOUND"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_survivor.inquiry")
    void mapsDatabaseFailureToSafeUnavailableProblem() throws Exception {
        when(inquiryService.inquire("CLM000000001", "BENE000001"))
                .thenThrow(new DataAccessResourceFailureException("sensitive database detail"));

        mockMvc.perform(get("/api/v1/survivor-entitlements/CLM000000001/beneficiaries/BENE000001"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("SURV-SERVICE-UNAVAILABLE"))
                .andExpect(jsonPath("$.detail").value("BENEFIT SERVICE UNAVAILABLE"));
    }
}