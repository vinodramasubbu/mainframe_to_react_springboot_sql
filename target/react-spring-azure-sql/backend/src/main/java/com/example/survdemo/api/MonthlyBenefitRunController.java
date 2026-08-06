package com.example.survdemo.api;

import com.example.survdemo.application.MonthlyBatchCommand;
import com.example.survdemo.application.OnDemandMonthlyBatchService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/monthly-benefit-runs", produces = MediaType.APPLICATION_JSON_VALUE)
public class MonthlyBenefitRunController {

    private final OnDemandMonthlyBatchService batchService;

    public MonthlyBenefitRunController(OnDemandMonthlyBatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_survivor.batch.run')")
    public MonthlyBenefitRunResponse run(@Valid @RequestBody MonthlyBenefitRunRequest request) {
        return MonthlyBenefitRunResponse.from(batchService.run(
                new MonthlyBatchCommand(request.runId(), request.calculationDate())));
    }
}