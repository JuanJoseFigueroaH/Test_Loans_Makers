package com.bankloan.infrastructure.adapter.in.rest;

import com.bankloan.application.dto.LoanResponse;
import com.bankloan.application.dto.RejectLoanRequest;
import com.bankloan.application.mapper.LoanMapper;
import com.bankloan.domain.model.*;
import com.bankloan.domain.port.in.ApproveLoanUseCase;
import com.bankloan.domain.port.in.GetAdminLoansUseCase;
import com.bankloan.domain.port.in.GetLoanSummaryUseCase;
import com.bankloan.domain.port.in.RejectLoanUseCase;
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
class AdminLoanControllerTest {

    @Mock private ApproveLoanUseCase approveLoanUseCase;
    @Mock private RejectLoanUseCase rejectLoanUseCase;
    @Mock private GetAdminLoansUseCase getAdminLoansUseCase;
    @Mock private GetLoanSummaryUseCase getLoanSummaryUseCase;
    @Mock private LoanMapper loanMapper;
    @Mock private Authentication authentication;

    @InjectMocks private AdminLoanController controller;

    private final UUID adminId = UUID.randomUUID();
    private final UUID loanId = UUID.randomUUID();
    private Loan testLoan;
    private LoanResponse testLoanResponse;

    @BeforeEach
    void setUp() {
        testLoan = Loan.reconstitute(loanId, UUID.randomUUID(), BigDecimal.valueOf(5000000), 12,
                LoanStatus.PENDING, Instant.now(), Instant.now(), null, null, null, 0L);
        testLoanResponse = new LoanResponse(loanId.toString(), testLoan.getUserId().toString(),
                BigDecimal.valueOf(5000000), 12, "PENDING", Instant.now(), Instant.now(), null, null, null);
    }

    @Test
    void getAllLoans_shouldReturnPagedResponse() {
        var pageResult = PageResult.of(List.of(testLoan), 0, 10, 1, 1, true, true);
        when(getAdminLoansUseCase.getAllLoans(isNull(), isNull(), eq(0), eq(10))).thenReturn(pageResult);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.getAllLoans(null, null, 0, 10, authentication))
                .assertNext(response -> {
                    assertThat(response.content()).hasSize(1);
                    assertThat(response.totalElements()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void getAllLoans_withStatusFilter_shouldPassStatus() {
        var pageResult = PageResult.of(List.of(testLoan), 0, 10, 1, 1, true, true);
        when(getAdminLoansUseCase.getAllLoans(eq(LoanStatus.APPROVED), isNull(), eq(0), eq(10))).thenReturn(pageResult);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.getAllLoans("APPROVED", null, 0, 10, authentication))
                .assertNext(response -> assertThat(response.content()).hasSize(1))
                .verifyComplete();
    }

    @Test
    void getAllLoans_withSearch_shouldPassSearch() {
        var pageResult = PageResult.of(List.of(testLoan), 0, 10, 1, 1, true, true);
        when(getAdminLoansUseCase.getAllLoans(isNull(), eq("search-term"), eq(0), eq(10))).thenReturn(pageResult);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.getAllLoans(null, "search-term", 0, 10, authentication))
                .assertNext(response -> assertThat(response.content()).hasSize(1))
                .verifyComplete();
    }

    @Test
    void getLoanById_shouldReturnLoan() {
        when(getAdminLoansUseCase.getLoanById(loanId)).thenReturn(testLoan);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.getLoanById(loanId, authentication))
                .assertNext(response -> assertThat(response.id()).isEqualTo(loanId.toString()))
                .verifyComplete();
    }

    @Test
    void getSummary_shouldReturnSummary() {
        var summary = new LoanSummary(10, 5, 3, 2);
        when(getLoanSummaryUseCase.getSummary()).thenReturn(summary);

        StepVerifier.create(controller.getSummary(authentication))
                .assertNext(response -> {
                    assertThat(response.total()).isEqualTo(10);
                    assertThat(response.pending()).isEqualTo(5);
                    assertThat(response.approved()).isEqualTo(3);
                    assertThat(response.rejected()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void approveLoan_shouldReturnApprovedLoan() {
        when(authentication.getName()).thenReturn(adminId.toString());
        when(approveLoanUseCase.approveLoan(loanId, adminId)).thenReturn(testLoan);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.approveLoan(loanId, authentication))
                .assertNext(response -> assertThat(response.id()).isEqualTo(loanId.toString()))
                .verifyComplete();
    }

    @Test
    void rejectLoan_shouldReturnRejectedLoan() {
        var request = new RejectLoanRequest("Does not meet criteria");
        when(authentication.getName()).thenReturn(adminId.toString());
        when(rejectLoanUseCase.rejectLoan(loanId, adminId, "Does not meet criteria")).thenReturn(testLoan);
        when(loanMapper.toResponse(testLoan)).thenReturn(testLoanResponse);

        StepVerifier.create(controller.rejectLoan(loanId, request, authentication))
                .assertNext(response -> assertThat(response.id()).isEqualTo(loanId.toString()))
                .verifyComplete();
    }
}
