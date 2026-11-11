package com.bankloan.application.query;

import java.util.UUID;

public record GetLoanByIdQuery(
        UUID loanId,
        UUID userId
) {}
