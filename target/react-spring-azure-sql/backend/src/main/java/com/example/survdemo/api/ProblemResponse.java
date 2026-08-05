package com.example.survdemo.api;

public record ProblemResponse(
        String type,
        String title,
        int status,
        String detail,
        String code,
        String correlationId) {
}