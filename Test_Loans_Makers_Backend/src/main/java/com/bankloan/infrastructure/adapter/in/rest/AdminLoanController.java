package com.bankloan.infrastructure.adapter.in.rest;

import com.bankloan.application.dto.LoanResponse;
import com.bankloan.application.dto.LoanSummaryResponse;
import com.bankloan.application.dto.PagedResponse;
import com.bankloan.application.dto.RejectLoanRequest;
import com.bankloan.application.mapper.LoanMapper;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.LoanSummary;
import com.bankloan.domain.model.PageResult;
import com.bankloan.domain.port.in.ApproveLoanUseCase;
import com.bankloan.domain.port.in.GetAdminLoansUseCase;
import com.bankloan.domain.port.in.GetLoanSummaryUseCase;
import com.bankloan.domain.port.in.RejectLoanUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/loans")
public class AdminLoanController {

    private final ApproveLoanUseCase approveLoanUseCase;
    private final RejectLoanUseCase rejectLoanUseCase;
    private final GetAdminLoansUseCase getAdminLoansUseCase;
    private final GetLoanSummaryUseCase getLoanSummaryUseCase;
    private final LoanMapper loanMapper;

    public AdminLoanController(ApproveLoanUseCase approveLoanUseCase,
                               RejectLoanUseCase rejectLoanUseCase,
                               GetAdminLoansUseCase getAdminLoansUseCase,
                               GetLoanSummaryUseCase getLoanSummaryUseCase,
                               LoanMapper loanMapper) {
        this.approveLoanUseCase = approveLoanUseCase;
        this.rejectLoanUseCase = rejectLoanUseCase;
        this.getAdminLoansUseCase = getAdminLoansUseCase;
        this.getLoanSummaryUseCase = getLoanSummaryUseCase;
        this.loanMapper = loanMapper;
    }

    @GetMapping
    public Mono<PagedResponse<LoanResponse>> getAllLoans(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return Mono.fromCallable(() -> {
            LoanStatus loanStatus = status != null ? LoanStatus.valueOf(status.toUpperCase()) : null;
            PageResult<Loan> loanPage = getAdminLoansUseCase.getAllLoans(loanStatus, search, page, size);
            List<LoanResponse> content = loanPage.content().stream()
                    .map(loanMapper::toResponse)
                    .collect(Collectors.toList());
            return new PagedResponse<>(
                    content,
                    loanPage.page(),
                    loanPage.size(),
                    loanPage.totalElements(),
                    loanPage.totalPages(),
                    loanPage.first(),
                    loanPage.last()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<LoanResponse> getLoanById(@PathVariable UUID id, Authentication authentication) {
        return Mono.fromCallable(() -> {
            Loan loan = getAdminLoansUseCase.getLoanById(id);
            return loanMapper.toResponse(loan);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/summary")
    public Mono<LoanSummaryResponse> getSummary(Authentication authentication) {
        return Mono.fromCallable(() -> {
            LoanSummary summary = getLoanSummaryUseCase.getSummary();
            return new LoanSummaryResponse(summary.total(), summary.pending(), summary.approved(), summary.rejected());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PatchMapping("/{id}/approve")
    public Mono<LoanResponse> approveLoan(@PathVariable UUID id, Authentication authentication) {
        return Mono.fromCallable(() -> {
            UUID adminId = UUID.fromString(authentication.getName());
            Loan loan = approveLoanUseCase.approveLoan(id, adminId);
            return loanMapper.toResponse(loan);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PatchMapping("/{id}/reject")
    public Mono<LoanResponse> rejectLoan(@PathVariable UUID id,
                                          @Valid @RequestBody RejectLoanRequest request,
                                          Authentication authentication) {
        return Mono.fromCallable(() -> {
            UUID adminId = UUID.fromString(authentication.getName());
            Loan loan = rejectLoanUseCase.rejectLoan(id, adminId, request.reason());
            return loanMapper.toResponse(loan);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
