package com.bankloan.application.query;

import com.bankloan.domain.model.LoanStatus;

import java.util.UUID;

public record GetMyLoansQuery(
        UUID userId,
        LoanStatus status,
        int page,
        int size
) {}
