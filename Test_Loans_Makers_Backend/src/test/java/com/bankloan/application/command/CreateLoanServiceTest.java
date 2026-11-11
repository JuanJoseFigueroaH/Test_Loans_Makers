package com.bankloan.application.command;

import com.bankloan.domain.exception.InvalidLoanException;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.port.out.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateLoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    private CreateLoanService service;

    @BeforeEach
    void setUp() {
        service = new CreateLoanService(loanRepository);
    }

    @Test
    void createLoan_withValidData_shouldPersistAndReturnLoan() {
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("15000000.00");
        Integer termMonths = 36;

        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = service.createLoan(userId, amount, termMonths);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAmount()).isEqualByComparingTo(amount);
        assertThat(result.getTermMonths()).isEqualTo(termMonths);
        assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    void createLoan_withZeroAmount_shouldThrowInvalidLoanException() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.createLoan(userId, BigDecimal.ZERO, 12))
                .isInstanceOf(InvalidLoanException.class)
                .hasMessageContaining("Amount must be greater than zero");

        verifyNoInteractions(loanRepository);
    }

    @Test
    void createLoan_withNegativeAmount_shouldThrowInvalidLoanException() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.createLoan(userId, new BigDecimal("-500"), 12))
                .isInstanceOf(InvalidLoanException.class)
                .hasMessageContaining("Amount must be greater than zero");

        verifyNoInteractions(loanRepository);
    }

    @Test
    void createLoan_withNullAmount_shouldThrowInvalidLoanException() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.createLoan(userId, null, 12))
                .isInstanceOf(InvalidLoanException.class)
                .hasMessageContaining("Amount must be greater than zero");

        verifyNoInteractions(loanRepository);
    }

    @Test
    void createLoan_withZeroTermMonths_shouldThrowInvalidLoanException() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.createLoan(userId, new BigDecimal("5000"), 0))
                .isInstanceOf(InvalidLoanException.class)
                .hasMessageContaining("Term months must be greater than zero");

        verifyNoInteractions(loanRepository);
    }

    @Test
    void createLoan_withNegativeTermMonths_shouldThrowInvalidLoanException() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.createLoan(userId, new BigDecimal("5000"), -3))
                .isInstanceOf(InvalidLoanException.class)
                .hasMessageContaining("Term months must be greater than zero");

        verifyNoInteractions(loanRepository);
    }

    @Test
    void createLoan_whenRepositoryFails_shouldPropagateException() {
        UUID userId = UUID.randomUUID();
        when(loanRepository.save(any(Loan.class))).thenThrow(new RuntimeException("DB connection lost"));

        assertThatThrownBy(() -> service.createLoan(userId, new BigDecimal("10000"), 12))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB connection lost");
    }

    @Test
    void createLoan_withNullTermMonths_shouldThrowInvalidLoanException() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.createLoan(userId, new BigDecimal("5000"), null))
                .isInstanceOf(InvalidLoanException.class)
                .hasMessageContaining("Term months must be greater than zero");

        verifyNoInteractions(loanRepository);
    }

    @Test
    void createLoan_shouldSetInitialStatusToPending() {
        UUID userId = UUID.randomUUID();
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = service.createLoan(userId, new BigDecimal("50000"), 24);

        assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(result.getReviewedAt()).isNull();
        assertThat(result.getReviewedBy()).isNull();
        assertThat(result.getRejectionReason()).isNull();
    }
}
