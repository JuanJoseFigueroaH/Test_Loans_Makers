package com.bankloan.application.command;

import java.util.UUID;

public record RejectLoanCommand(
        UUID loanId,
        UUID adminId,
        String reason
) {}
