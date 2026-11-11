package com.bankloan.application.query;

import com.bankloan.domain.exception.LoanNotFoundException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
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
class LoanQueryServiceTest {

    @Mock
    private LoanRepository loanRepository;

    private LoanQueryService service;

    @BeforeEach
    void setUp() {
        service = new LoanQueryService(loanRepository);
    }

    @Test
    void getMyLoans_shouldReturnPaginatedLoans() {
        UUID userId = UUID.randomUUID();
        Loan loan = Loan.create(userId, new BigDecimal("10000"), 12);
        PageResult<Loan> page = PageResult.of(List.of(loan), 0, 10, 1, 1, true, true);

        when(loanRepository.findByUserIdPaginated(userId, null, 0, 10)).thenReturn(page);

        PageResult<Loan> result = service.getMyLoans(userId, null, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getUserId()).isEqualTo(userId);
        verify(loanRepository).findByUserIdPaginated(userId, null, 0, 10);
    }

    @Test
    void getMyLoans_withStatusFilter_shouldPassStatusToRepository() {
        UUID userId = UUID.randomUUID();
        PageResult<Loan> page = PageResult.empty();

        when(loanRepository.findByUserIdPaginated(userId, LoanStatus.PENDING, 0, 10)).thenReturn(page);

        PageResult<Loan> result = service.getMyLoans(userId, LoanStatus.PENDING, 0, 10);

        assertThat(result.content()).isEmpty();
        verify(loanRepository).findByUserIdPaginated(userId, LoanStatus.PENDING, 0, 10);
    }

    @Test
    void getLoanById_withOwnLoan_shouldReturnLoan() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Loan loan = Loan.create(userId, new BigDecimal("5000"), 24);

        when(loanRepository.findByIdAndUserId(loanId, userId)).thenReturn(Optional.of(loan));

        Loan result = service.getLoanById(loanId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void getLoanById_notFound_shouldThrowLoanNotFoundException() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(loanRepository.findByIdAndUserId(loanId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLoanById(loanId, userId))
                .isInstanceOf(LoanNotFoundException.class);
    }

    @Test
    void getLoanById_otherUsersLoan_shouldThrowNotFoundException() {
        UUID loanId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        when(loanRepository.findByIdAndUserId(loanId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLoanById(loanId, otherUserId))
                .isInstanceOf(LoanNotFoundException.class);
    }
}
