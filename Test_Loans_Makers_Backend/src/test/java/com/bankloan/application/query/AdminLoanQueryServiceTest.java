package com.bankloan.application.query;

import com.bankloan.domain.exception.LoanNotFoundException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.LoanSummary;
import com.bankloan.domain.model.PageResult;
import com.bankloan.domain.port.out.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminLoanQueryServiceTest {

    @Mock
    private LoanRepository loanRepository;

    private AdminLoanQueryService service;

    @BeforeEach
    void setUp() {
        service = new AdminLoanQueryService(loanRepository);
    }

    @Test
    void getAllLoans_withoutFilters_shouldReturnPaginatedLoans() {
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        PageResult<Loan> page = PageResult.of(List.of(loan), 0, 10, 1, 1, true, true);

        when(loanRepository.findAllPaginated(null, 0, 10)).thenReturn(page);

        PageResult<Loan> result = service.getAllLoans(null, null, 0, 10);

        assertThat(result.content()).hasSize(1);
        verify(loanRepository).findAllPaginated(null, 0, 10);
    }

    @Test
    void getAllLoans_withStatusFilter_shouldPassStatusToRepository() {
        PageResult<Loan> page = PageResult.empty();

        when(loanRepository.findAllPaginated(LoanStatus.PENDING, 0, 10)).thenReturn(page);

        PageResult<Loan> result = service.getAllLoans(LoanStatus.PENDING, null, 0, 10);

        assertThat(result.content()).isEmpty();
        verify(loanRepository).findAllPaginated(LoanStatus.PENDING, 0, 10);
    }

    @Test
    void getAllLoans_withValidSearchId_shouldReturnMatchingLoan() {
        UUID loanId = UUID.randomUUID();
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("5000"), 24);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        PageResult<Loan> result = service.getAllLoans(null, loanId.toString(), 0, 10);

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void getAllLoans_withInvalidSearchId_shouldReturnEmptyPage() {
        PageResult<Loan> result = service.getAllLoans(null, "not-a-uuid", 0, 10);

        assertThat(result.content()).isEmpty();
        verifyNoInteractions(loanRepository);
    }

    @Test
    void getAllLoans_withNonExistentSearchId_shouldReturnEmptyPage() {
        UUID loanId = UUID.randomUUID();

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        PageResult<Loan> result = service.getAllLoans(null, loanId.toString(), 0, 10);

        assertThat(result.content()).isEmpty();
    }

    @Test
    void getLoanById_existingLoan_shouldReturnLoan() {
        UUID loanId = UUID.randomUUID();
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("8000"), 36);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        Loan result = service.getLoanById(loanId);

        assertThat(result).isNotNull();
    }

    @Test
    void getLoanById_nonExistent_shouldThrowLoanNotFoundException() {
        UUID loanId = UUID.randomUUID();

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLoanById(loanId))
                .isInstanceOf(LoanNotFoundException.class);
    }

    @Test
    void getSummary_shouldReturnCorrectCounts() {
        when(loanRepository.countAll()).thenReturn(10L);
        when(loanRepository.countByStatus(LoanStatus.PENDING)).thenReturn(4L);
        when(loanRepository.countByStatus(LoanStatus.APPROVED)).thenReturn(3L);
        when(loanRepository.countByStatus(LoanStatus.REJECTED)).thenReturn(3L);

        LoanSummary summary = service.getSummary();

        assertThat(summary.total()).isEqualTo(10);
        assertThat(summary.pending()).isEqualTo(4);
        assertThat(summary.approved()).isEqualTo(3);
        assertThat(summary.rejected()).isEqualTo(3);
    }
}
