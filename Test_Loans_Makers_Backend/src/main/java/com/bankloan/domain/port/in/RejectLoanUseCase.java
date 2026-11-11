package com.bankloan.domain.port.in;

import com.bankloan.domain.model.Loan;

import java.util.UUID;

public interface RejectLoanUseCase {

    Loan rejectLoan(UUID loanId, UUID adminId, String reason);
}
