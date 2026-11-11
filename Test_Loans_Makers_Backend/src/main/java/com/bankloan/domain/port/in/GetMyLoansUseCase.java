package com.bankloan.domain.port.in;

import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.PageResult;

import java.util.UUID;

public interface GetMyLoansUseCase {

    PageResult<Loan> getMyLoans(UUID userId, LoanStatus status, int page, int size);
}
