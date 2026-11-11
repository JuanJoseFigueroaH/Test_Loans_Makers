package com.bankloan.domain.port.in;

import com.bankloan.domain.model.Loan;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreateLoanUseCase {

    Loan createLoan(UUID userId, BigDecimal amount, Integer termMonths);
}
