package com.bankloan.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanResponse(
        String id,
        String userId,
        BigDecimal amount,
        Integer termMonths,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Instant reviewedAt,
        String reviewedBy,
        String rejectionReason
) {}
