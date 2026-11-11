package com.bankloan.domain.exception;

public abstract sealed class DomainException extends RuntimeException
        permits InvalidCredentialsException, InvalidLoanException, InvalidLoanTransitionException,
                LoanConcurrencyException, LoanNotFoundException, UserDisabledException, UserNotFoundException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
