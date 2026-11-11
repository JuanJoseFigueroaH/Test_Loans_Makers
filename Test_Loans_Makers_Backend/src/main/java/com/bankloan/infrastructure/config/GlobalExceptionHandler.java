package com.bankloan.infrastructure.config;

import com.bankloan.domain.exception.DomainException;
import com.bankloan.domain.exception.InvalidCredentialsException;
import com.bankloan.domain.exception.InvalidLoanException;
import com.bankloan.domain.exception.InvalidLoanTransitionException;
import com.bankloan.domain.exception.LoanConcurrencyException;
import com.bankloan.domain.exception.LoanNotFoundException;
import com.bankloan.domain.exception.UserDisabledException;
import com.bankloan.domain.exception.UserNotFoundException;
import com.bankloan.infrastructure.adapter.out.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_PREFIX = "https://api.bankloan.com/errors/";

    @ExceptionHandler(DomainException.class)
    public Mono<ProblemDetail> handleDomainException(DomainException ex) {
        record Mapping(HttpStatus status, String title, String typeSuffix) {}

        Mapping mapping = switch (ex) {
            case InvalidCredentialsException e    -> new Mapping(HttpStatus.UNAUTHORIZED, "Invalid Credentials", "authentication/invalid-credentials");
            case UserDisabledException e          -> new Mapping(HttpStatus.FORBIDDEN, "Account Disabled", "authentication/account-disabled");
            case UserNotFoundException e          -> new Mapping(HttpStatus.NOT_FOUND, "User Not Found", "users/not-found");
            case InvalidLoanException e           -> new Mapping(HttpStatus.BAD_REQUEST, "Invalid Loan Request", "loans/invalid-request");
            case LoanNotFoundException e          -> new Mapping(HttpStatus.NOT_FOUND, "Loan Not Found", "loans/not-found");
            case InvalidLoanTransitionException e -> new Mapping(HttpStatus.CONFLICT, "Invalid Loan Transition", "loans/invalid-transition");
            case LoanConcurrencyException e       -> new Mapping(HttpStatus.CONFLICT, "Concurrency Conflict", "loans/concurrency-conflict");
        };

        if (mapping.status().is5xxServerError()) {
            log.error("{}: {}", mapping.title(), ex.getMessage(), ex);
        } else {
            log.warn("{}: {}", mapping.title(), ex.getMessage());
        }

        return Mono.just(buildProblem(mapping.status(), mapping.title(), mapping.typeSuffix(), ex.getMessage(), ex.getCode()));
    }

    @ExceptionHandler(JwtTokenProvider.InvalidTokenException.class)
    public Mono<ProblemDetail> handleInvalidToken(JwtTokenProvider.InvalidTokenException ex) {
        log.warn("Invalid JWT token: {}", ex.getMessage());
        return Mono.just(buildProblem(
                HttpStatus.UNAUTHORIZED,
                "Invalid Token",
                "authentication/invalid-token",
                ex.getMessage(),
                "INVALID_TOKEN"
        ));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ProblemDetail> handleValidation(WebExchangeBindException ex) {
        List<Map<String, String>> errors = ex.getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value"
                ))
                .collect(Collectors.toList());

        log.warn("Validation failed: {} field error(s)", errors.size());
        ProblemDetail detail = buildProblem(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "validation/fields-invalid",
                "One or more fields failed validation",
                "VALIDATION_ERROR"
        );
        detail.setProperty("errors", errors);
        return Mono.just(detail);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ProblemDetail> handleBadInput(ServerWebInputException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return Mono.just(buildProblem(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "validation/malformed-input",
                "The request body is malformed or missing required fields",
                "BAD_INPUT"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return Mono.just(buildProblem(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "validation/illegal-argument",
                ex.getMessage(),
                "ILLEGAL_ARGUMENT"
        ));
    }

    @ExceptionHandler(DataAccessException.class)
    public Mono<ProblemDetail> handleDataAccess(DataAccessException ex) {
        log.error("Database error: {}", ex.getMessage(), ex);
        return Mono.just(buildProblem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Temporarily Unavailable",
                "infrastructure/database-error",
                "A temporary database error occurred. Please try again later.",
                "DATABASE_ERROR"
        ));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Mono.just(buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "internal/unexpected",
                "An unexpected error occurred. Please contact support if the problem persists.",
                "INTERNAL_ERROR"
        ));
    }

    private ProblemDetail buildProblem(HttpStatus status, String title, String typeSuffix,
                                       String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(TYPE_PREFIX + typeSuffix));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
