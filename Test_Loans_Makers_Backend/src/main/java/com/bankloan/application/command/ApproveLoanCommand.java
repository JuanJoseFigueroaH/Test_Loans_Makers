package com.bankloan.application.command;

import java.util.UUID;

public record ApproveLoanCommand(
        UUID loanId,
        UUID adminId
) {}
