package com.bankloan.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectLoanRequest(
        @NotBlank(message = "Rejection reason is required")
        String reason
) {}
