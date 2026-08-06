package com.example.survdemo.api;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.MonthlyBatchException;
import com.example.survdemo.application.MonthlyBatchExecutionResult;
import com.example.survdemo.application.MonthlyBatchOutcome;
import com.example.survdemo.application.MonthlyBatchPayment;
import com.example.survdemo.application.MonthlyBatchPaymentResult;
import com.example.survdemo.application.OnDemandMonthlyBatchService;
import com.example.survdemo.config.SecurityConfig;
import com.example.survdemo.domain.BeneficiaryId;
import com.example.survdemo.domain.ClaimId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = {
                MonthlyBenefitRunController.class,
                ApiExceptionHandler.class,
                CorrelationIdFilter.class,
                SecurityProblemWriter.class,
                SecurityConfig.class
        },
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example",
                "survdemo.security.allowed-origin=https://localhost:5173"
        })
class MonthlyBenefitRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OnDemandMonthlyBatchService batchService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(authorities = "SCOPE_survivor.batch.run")
    void returnsStructuredRunOutcomeForAuthorizedOperator() throws Exception {
        MonthlyBatchCommand command = new MonthlyBatchCommand(
                "SRV202608001", LocalDate.of(2026, 8, 31));
        MonthlyBatchPayment payment = new MonthlyBatchPayment(
                "SRV2026080010001", command.runId(),
                new ClaimId("CLM000000001"), new BeneficiaryId("BENE000001"),
                command.benefitMonth(), new BigDecimal("600.00"), new BigDecimal("25.50"),
                new BigDecimal("574.50"), "R");
        MonthlyBatchException exception = new MonthlyBatchException(
                command.runId(), new ClaimId("CLM000000002"), new BeneficiaryId("BENE000002"),
                "F1", new BigDecimal("500.00"), new BigDecimal("0.00"));
        when(batchService.run(command)).thenReturn(new MonthlyBatchExecutionResult(
                command,
                new MonthlyBatchOutcome(4, 1, new BigDecimal("574.50"), 1),
                List.of(new MonthlyBatchPaymentResult(payment, "SPS", "JANE DOE")),
                List.of(exception)));

        mockMvc.perform(post("/api/v1/monthly-benefit-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"runId":"SRV202608001","calculationDate":"2026-08-31"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnCode").value(4))
                .andExpect(jsonPath("$.outcome").value("COMPLETED_WITH_EXCEPTIONS"))
                .andExpect(jsonPath("$.paymentTotal").value("574.50"))
                .andExpect(jsonPath("$.payments[0].beneficiaryName").value("JANE DOE"))
                .andExpect(jsonPath("$.payments[0].netAmount").value("574.50"))
                .andExpect(jsonPath("$.exceptions[0].reasonCode").value("F1"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_survivor.batch.run")
    void rejectsMalformedRunIdBeforeStartingBatch() throws Exception {
        mockMvc.perform(post("/api/v1/monthly-benefit-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"runId":"SHORT","calculationDate":"2026-08-31"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SURV-BATCH-VALIDATION"));

        verify(batchService, never()).run(any());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_survivor.inquiry")
    void requiresBatchRunScope() throws Exception {
        mockMvc.perform(post("/api/v1/monthly-benefit-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"runId":"SRV202608001","calculationDate":"2026-08-31"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SURV-FORBIDDEN"));
    }
}