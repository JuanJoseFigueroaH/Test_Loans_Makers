package com.bankloan.infrastructure.adapter.in.rest;

import com.bankloan.application.dto.CreateLoanRequest;
import com.bankloan.application.dto.LoanResponse;
import com.bankloan.application.dto.PagedResponse;
import com.bankloan.application.mapper.LoanMapper;
import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.PageResult;
import com.bankloan.domain.port.in.CreateLoanUseCase;
import com.bankloan.domain.port.in.GetLoanByIdUseCase;
import com.bankloan.domain.port.in.GetMyLoansUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final CreateLoanUseCase createLoanUseCase;
    private final GetMyLoansUseCase getMyLoansUseCase;
    private final GetLoanByIdUseCase getLoanByIdUseCase;
    private final LoanMapper loanMapper;

    public LoanController(CreateLoanUseCase createLoanUseCase,
                           GetMyLoansUseCase getMyLoansUseCase,
                           GetLoanByIdUseCase getLoanByIdUseCase,
                           LoanMapper loanMapper) {
        this.createLoanUseCase = createLoanUseCase;
        this.getMyLoansUseCase = getMyLoansUseCase;
        this.getLoanByIdUseCase = getLoanByIdUseCase;
        this.loanMapper = loanMapper;
    }

    @PostMapping
    public Mono<ResponseEntity<LoanResponse>> createLoan(
            @Valid @RequestBody CreateLoanRequest request,
            Authentication authentication) {
        return Mono.fromCallable(() -> {
            UUID userId = UUID.fromString(authentication.getName());
            Loan loan = createLoanUseCase.createLoan(userId, request.amount(), request.termMonths());
            LoanResponse response = loanMapper.toResponse(loan);
            URI location = URI.create("/api/v1/loans/" + loan.getId());
            return ResponseEntity.created(location).body(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping
    public Mono<PagedResponse<LoanResponse>> getMyLoans(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return Mono.fromCallable(() -> {
            UUID userId = UUID.fromString(authentication.getName());
            LoanStatus loanStatus = status != null ? LoanStatus.valueOf(status.toUpperCase()) : null;
            PageResult<Loan> loanPage = getMyLoansUseCase.getMyLoans(userId, loanStatus, page, size);
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
            UUID userId = UUID.fromString(authentication.getName());
            Loan loan = getLoanByIdUseCase.getLoanById(id, userId);
            return loanMapper.toResponse(loan);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
