package com.bankloan.application.query;

import com.bankloan.domain.exception.LoanNotFoundException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.port.in.GetLoanByIdUseCase;
import com.bankloan.domain.port.in.GetMyLoansUseCase;
import com.bankloan.domain.model.PageResult;
import com.bankloan.domain.port.out.LoanRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.bankloan.application.config.CacheNames.LOAN_BY_ID;

@Service
@Transactional(readOnly = true)
public class LoanQueryService implements GetMyLoansUseCase, GetLoanByIdUseCase {

    private final LoanRepository loanRepository;

    public LoanQueryService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public PageResult<Loan> getMyLoans(UUID userId, LoanStatus status, int page, int size) {
        return loanRepository.findByUserIdPaginated(userId, status, page, size);
    }

    @Override
    @Cacheable(value = LOAN_BY_ID, key = "#loanId")
    public Loan getLoanById(UUID loanId, UUID userId) {
        return loanRepository.findByIdAndUserId(loanId, userId)
                .orElseThrow(() -> new LoanNotFoundException(loanId.toString()));
    }
}
