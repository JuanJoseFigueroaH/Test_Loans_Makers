package com.bankloan.application.command;

import com.bankloan.domain.exception.InvalidLoanTransitionException;
import com.bankloan.domain.exception.LoanConcurrencyException;
import com.bankloan.domain.exception.LoanNotFoundException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.port.in.ApproveLoanUseCase;
import com.bankloan.domain.port.out.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.bankloan.application.config.CacheNames.*;

@Service
public class LoanApprovalService implements ApproveLoanUseCase {

    private final LoanRepository loanRepository;

    public LoanApprovalService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(LoanApprovalService.class);

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Caching(evict = {
            @CacheEvict(value = LOAN_BY_ID, key = "#loanId"),
            @CacheEvict(value = USER_LOANS, allEntries = true),
            @CacheEvict(value = ADMIN_LOANS, allEntries = true),
            @CacheEvict(value = LOAN_SUMMARY, allEntries = true)
    })
    public Loan approveLoan(UUID loanId, UUID adminId) {
        log.info("Processing loan approval: loanId={}, adminId={}", loanId, adminId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId.toString()));

        if (!loan.isPending()) {
            throw new InvalidLoanTransitionException(
                    "Loan " + loanId + " is already " + loan.getStatus() + " and cannot be approved");
        }

        try {
            loan.approve(adminId);
            Loan saved = loanRepository.saveAndFlush(loan);
            log.info("Loan approved successfully: loanId={}, adminId={}", loanId, adminId);
            return saved;
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock conflict on loan approval: loanId={}, adminId={}", loanId, adminId);
            throw new LoanConcurrencyException(
                    "Loan " + loanId + " was modified by another administrator. Please refresh and try again.");
        }
    }
}
