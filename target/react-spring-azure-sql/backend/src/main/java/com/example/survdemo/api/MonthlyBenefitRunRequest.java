package com.example.survdemo.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MonthlyBenefitRunRequest(
        @Size(min = 12, max = 12)
        @Pattern(regexp = "\\S{12}")
        String runId,
        @NotNull LocalDate calculationDate) {
}