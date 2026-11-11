package com.bankloan.infrastructure.adapter.in.rest;

import com.bankloan.application.dto.CreateLoanRequest;
import com.bankloan.application.dto.LoanResponse;
import com.bankloan.application.mapper.LoanMapper;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.PageResult;
import com.bankloan.domain.port.in.CreateLoanUseCase;
import com.bankloan.domain.port.in.GetLoanByIdUseCase;
import com.bankloan.domain.port.in.GetMyLoansUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    @Mock private CreateLoanUseCase createLoanUseCase;
    @Mock private GetMyLoansUseCase getMyLoansUseCase;
    @Mock private GetLoanByIdUseCase getLoanByIdUseCase;
    @Mock private LoanMapper loanMapper;
    @Mock private Authentication authentication;

    @InjectMocks private LoanController controller;

    private final UUID userId = UUID.randomUUID();
    private final UUID loanId = UUID.randomUUID();
    private Loan testLoan;
    private LoanResponse testLoanResponse;

    @BeforeEach
    void setUp() {
        testLoan = Loan.reconstitute(loanId, userId, BigDecimal.valueOf(5000000), 12,
                LoanStatus.PENDING, Instant.now(), Instant.now(), null, null, null, 0L);
        testLoanResponse = new LoanResponse(loanId.toString(), userId.toString(),
                BigDecimal.valueOf(5000000), 12, "PENDING", Instant.now(), Instant.now(), null, null, null);
    }

    @Test
    void createLoan_shouldReturnCreatedResponse() {
        var request = new CreateLoanRequest(BigDecimal.valueOf(5000000), 12);
        when(authentication.getName()).thenReturn(userId.toString());
        when(createLoanUseCase.createLoan(eq(userId), eq(BigDecimal.valueOf(5000000)), eq(12))).thenReturn(testLoan);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.createLoan(request, authentication))
                .assertNext(response -> {
                    assertThat(response.getStatusCode().value()).isEqualTo(201);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().status()).isEqualTo("PENDING");
                })
                .verifyComplete();
    }

    @Test
    void getMyLoans_shouldReturnPagedResponse() {
        var pageResult = PageResult.of(List.of(testLoan), 0, 10, 1, 1, true, true);
        when(authentication.getName()).thenReturn(userId.toString());
        when(getMyLoansUseCase.getMyLoans(eq(userId), isNull(), eq(0), eq(10))).thenReturn(pageResult);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.getMyLoans(null, 0, 10, authentication))
                .assertNext(response -> {
                    assertThat(response.content()).hasSize(1);
                    assertThat(response.totalElements()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void getMyLoans_withStatusFilter_shouldPassStatus() {
        var pageResult = PageResult.of(List.of(testLoan), 0, 10, 1, 1, true, true);
        when(authentication.getName()).thenReturn(userId.toString());
        when(getMyLoansUseCase.getMyLoans(eq(userId), eq(LoanStatus.PENDING), eq(0), eq(10))).thenReturn(pageResult);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.getMyLoans("PENDING", 0, 10, authentication))
                .assertNext(response -> assertThat(response.content()).hasSize(1))
                .verifyComplete();
    }

    @Test
    void getLoanById_shouldReturnLoan() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(getLoanByIdUseCase.getLoanById(loanId, userId)).thenReturn(testLoan);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.getLoanById(loanId, authentication))
                .assertNext(response -> assertThat(response.id()).isEqualTo(loanId.toString()))
                .verifyComplete();
    }
}
