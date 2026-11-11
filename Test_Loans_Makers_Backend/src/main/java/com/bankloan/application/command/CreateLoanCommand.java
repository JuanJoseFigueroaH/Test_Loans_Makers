package com.bankloan.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanCommand(
        UUID userId,
        BigDecimal amount,
        Integer termMonths
) {}
