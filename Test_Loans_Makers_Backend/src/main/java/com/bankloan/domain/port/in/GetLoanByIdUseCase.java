package com.bankloan.domain.port.in;

import com.bankloan.domain.model.Loan;

import java.util.UUID;

public interface GetLoanByIdUseCase {

    Loan getLoanById(UUID loanId, UUID userId);
}
