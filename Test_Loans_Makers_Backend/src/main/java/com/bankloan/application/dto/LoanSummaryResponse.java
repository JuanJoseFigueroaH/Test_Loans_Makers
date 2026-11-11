package com.bankloan.application.dto;

import java.io.Serializable;

public record LoanSummaryResponse(
        long total,
        long pending,
        long approved,
        long rejected
) implements Serializable {}
