package com.bankloan.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateLoanRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @DecimalMax(value = "999999999.99", message = "Amount must not exceed 999,999,999.99")
        BigDecimal amount,

        @NotNull(message = "Term months is required")
        @Min(value = 1, message = "Term months must be greater than zero")
        @Max(value = 360, message = "Term months must not exceed 360")
        Integer termMonths
) {}
