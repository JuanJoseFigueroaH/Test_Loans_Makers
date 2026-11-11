package com.bankloan.application.command;

import com.bankloan.domain.exception.InvalidLoanException;
import com.bankloan.domain.exception.InvalidLoanTransitionException;
import com.bankloan.domain.exception.LoanConcurrencyException;
import com.bankloan.domain.exception.LoanNotFoundException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.port.in.RejectLoanUseCase;
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
public class LoanRejectionService implements RejectLoanUseCase {

    private final LoanRepository loanRepository;

    public LoanRejectionService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(LoanRejectionService.class);

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Caching(evict = {
            @CacheEvict(value = LOAN_BY_ID, key = "#loanId"),
            @CacheEvict(value = USER_LOANS, allEntries = true),
            @CacheEvict(value = ADMIN_LOANS, allEntries = true),
            @CacheEvict(value = LOAN_SUMMARY, allEntries = true)
    })
    public Loan rejectLoan(UUID loanId, UUID adminId, String reason) {
        log.info("Processing loan rejection: loanId={}, adminId={}", loanId, adminId);

        if (reason == null || reason.isBlank()) {
            throw new InvalidLoanException("Rejection reason is required");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId.toString()));

        if (!loan.isPending()) {
            throw new InvalidLoanTransitionException(
                    "Loan " + loanId + " is already " + loan.getStatus() + " and cannot be rejected");
        }

        try {
            loan.reject(adminId, reason);
            Loan saved = loanRepository.saveAndFlush(loan);
            log.info("Loan rejected successfully: loanId={}, adminId={}, reason='{}'", loanId, adminId, reason);
            return saved;
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock conflict on loan rejection: loanId={}, adminId={}", loanId, adminId);
            throw new LoanConcurrencyException(
                    "Loan " + loanId + " was modified by another administrator. Please refresh and try again.");
        }
    }
}
