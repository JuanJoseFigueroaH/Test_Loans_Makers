package com.bankloan.application.command;

import com.bankloan.domain.exception.InvalidLoanTransitionException;
import com.bankloan.domain.exception.LoanConcurrencyException;
import com.bankloan.domain.exception.LoanNotFoundException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.port.out.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApprovalServiceTest {

    @Mock
    private LoanRepository loanRepository;

    private LoanApprovalService service;

    @BeforeEach
    void setUp() {
        service = new LoanApprovalService(loanRepository);
    }

    @Test
    void approveLoan_pendingLoan_shouldApproveAndPersist() {
        UUID loanId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.saveAndFlush(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = service.approveLoan(loanId, adminId);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(result.getReviewedBy()).isEqualTo(adminId);
        assertThat(result.getReviewedAt()).isNotNull();
        verify(loanRepository).saveAndFlush(any(Loan.class));
    }

    @Test
    void approveLoan_nonExistentLoan_shouldThrowNotFoundException() {
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveLoan(loanId, UUID.randomUUID()))
                .isInstanceOf(LoanNotFoundException.class);
    }

    @Test
    void approveLoan_alreadyApproved_shouldThrowTransitionException() {
        UUID loanId = UUID.randomUUID();
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        loan.approve(UUID.randomUUID());

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.approveLoan(loanId, UUID.randomUUID()))
                .isInstanceOf(InvalidLoanTransitionException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    void approveLoan_alreadyRejected_shouldThrowTransitionException() {
        UUID loanId = UUID.randomUUID();
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);
        loan.reject(UUID.randomUUID(), "reason");

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.approveLoan(loanId, UUID.randomUUID()))
                .isInstanceOf(InvalidLoanTransitionException.class)
                .hasMessageContaining("REJECTED");
    }

    @Test
    void approveLoan_concurrencyConflict_shouldThrowConcurrencyException() {
        UUID loanId = UUID.randomUUID();
        Loan loan = Loan.create(UUID.randomUUID(), new BigDecimal("10000"), 12);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanRepository.saveAndFlush(any(Loan.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException("Loan", loanId));

        assertThatThrownBy(() -> service.approveLoan(loanId, UUID.randomUUID()))
                .isInstanceOf(LoanConcurrencyException.class)
                .hasMessageContaining("modified by another administrator");
    }
}
