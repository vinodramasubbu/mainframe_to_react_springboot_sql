package com.example.survdemo.api;

import com.example.survdemo.application.EntitlementNotFoundException;
import com.example.survdemo.domain.InvalidInquiryIdentifierException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

        @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
        ResponseEntity<ProblemResponse> invalidBatchRequest(Exception exception, HttpServletRequest request) {
                return problem(HttpStatus.BAD_REQUEST, "Invalid monthly run", "Check the run ID and calculation date",
                                "SURV-BATCH-VALIDATION", request);
        }

    @ExceptionHandler(InvalidInquiryIdentifierException.class)
    ResponseEntity<ProblemResponse> invalidIdentifier(
            InvalidInquiryIdentifierException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid inquiry", exception.getMessage(),
                "SURV-VALIDATION", request);
    }

    @ExceptionHandler(EntitlementNotFoundException.class)
    ResponseEntity<ProblemResponse> notFound(HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Entitlement not found", "ENTITLEMENT NOT FOUND",
                "SURV-ENTITLEMENT-NOT-FOUND", request);
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ProblemResponse> unavailable(DataAccessException exception, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.correlationId(request);
        LOGGER.error("Survivor inquiry database failure, correlationId={}", correlationId, exception);
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable", "BENEFIT SERVICE UNAVAILABLE",
                "SURV-SERVICE-UNAVAILABLE", request);
    }

        @ExceptionHandler(AccessDeniedException.class)
        ResponseEntity<ProblemResponse> forbidden(HttpServletRequest request) {
                return problem(HttpStatus.FORBIDDEN, "Access denied", "Access denied", "SURV-FORBIDDEN", request);
        }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemResponse> unexpected(Exception exception, HttpServletRequest request) {
        String correlationId = CorrelationIdFilter.correlationId(request);
        LOGGER.error("Unexpected survivor inquiry failure, correlationId={}", correlationId, exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error",
                "The request could not be completed", "SURV-UNEXPECTED", request);
    }

    private ResponseEntity<ProblemResponse> problem(
            HttpStatus status, String title, String detail, String code, HttpServletRequest request) {
        ProblemResponse body = new ProblemResponse(
                "https://survdemo.example/problems/" + code.toLowerCase(),
                title,
                status.value(),
                detail,
                code,
                CorrelationIdFilter.correlationId(request));
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}