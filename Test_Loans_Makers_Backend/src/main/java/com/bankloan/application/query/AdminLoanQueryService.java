package com.bankloan.application.query;

import com.bankloan.domain.exception.LoanNotFoundException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.LoanSummary;
import com.bankloan.domain.model.PageResult;
import com.bankloan.domain.port.in.GetAdminLoansUseCase;
import com.bankloan.domain.port.in.GetLoanSummaryUseCase;
import com.bankloan.domain.port.out.LoanRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.bankloan.application.config.CacheNames.LOAN_BY_ID;
import static com.bankloan.application.config.CacheNames.LOAN_SUMMARY;

@Service
@Transactional(readOnly = true)
public class AdminLoanQueryService implements GetAdminLoansUseCase, GetLoanSummaryUseCase {

    private final LoanRepository loanRepository;

    public AdminLoanQueryService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public PageResult<Loan> getAllLoans(LoanStatus status, String searchId, int page, int size) {
        if (searchId != null && !searchId.isBlank()) {
            try {
                UUID id = UUID.fromString(searchId.trim());
                return loanRepository.findById(id)
                        .map(loan -> PageResult.of(List.of(loan), 0, 1, 1, 1, true, true))
                        .orElse(PageResult.empty());
            } catch (IllegalArgumentException e) {
                return PageResult.empty();
            }
        }
        return loanRepository.findAllPaginated(status, page, size);
    }

    @Override
    @Cacheable(value = LOAN_BY_ID, key = "#loanId")
    public Loan getLoanById(UUID loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId.toString()));
    }

    @Override
    @Cacheable(value = LOAN_SUMMARY)
    public LoanSummary getSummary() {
        long total = loanRepository.countAll();
        long pending = loanRepository.countByStatus(LoanStatus.PENDING);
        long approved = loanRepository.countByStatus(LoanStatus.APPROVED);
        long rejected = loanRepository.countByStatus(LoanStatus.REJECTED);
        return new LoanSummary(total, pending, approved, rejected);
    }
}
