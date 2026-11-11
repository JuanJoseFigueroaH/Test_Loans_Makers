package com.bankloan.application.command;

import com.bankloan.domain.exception.InvalidLoanException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.port.in.CreateLoanUseCase;
import com.bankloan.domain.port.out.LoanRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.bankloan.application.config.CacheNames.*;

@Service
public class CreateLoanService implements CreateLoanUseCase {

    private final LoanRepository loanRepository;

    public CreateLoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_LOANS, allEntries = true),
            @CacheEvict(value = ADMIN_LOANS, allEntries = true),
            @CacheEvict(value = LOAN_SUMMARY, allEntries = true)
    })
    public Loan createLoan(UUID userId, BigDecimal amount, Integer termMonths) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanException("Amount must be greater than zero");
        }
        if (termMonths == null || termMonths <= 0) {
            throw new InvalidLoanException("Term months must be greater than zero");
        }

        Loan loan = Loan.create(userId, amount, termMonths);
        return loanRepository.save(loan);
    }
}
