package com.bankloan.domain.port.in;

import com.bankloan.domain.model.Loan;

import java.util.UUID;

public interface ApproveLoanUseCase {

    Loan approveLoan(UUID loanId, UUID adminId);
}
