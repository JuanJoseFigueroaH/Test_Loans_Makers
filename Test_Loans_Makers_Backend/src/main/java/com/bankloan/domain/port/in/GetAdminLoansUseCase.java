package com.bankloan.domain.port.in;

import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.PageResult;

import java.util.UUID;

public interface GetAdminLoansUseCase {

    PageResult<Loan> getAllLoans(LoanStatus status, String searchId, int page, int size);

    Loan getLoanById(UUID loanId);
}
