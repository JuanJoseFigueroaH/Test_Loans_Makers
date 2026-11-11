package com.bankloan.domain.exception;

public final class LoanNotFoundException extends DomainException {

    public LoanNotFoundException(String loanId) {
        super("LOAN_NOT_FOUND", "Loan not found with id: " + loanId);
    }
}
