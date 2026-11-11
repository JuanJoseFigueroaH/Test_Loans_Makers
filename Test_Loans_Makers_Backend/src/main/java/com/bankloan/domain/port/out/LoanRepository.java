package com.bankloan.domain.port.out;

import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository {

    Loan save(Loan loan);

    Loan saveAndFlush(Loan loan);

    Optional<Loan> findById(UUID id);

    Optional<Loan> findByIdAndUserId(UUID id, UUID userId);

    List<Loan> findByUserId(UUID userId);

    PageResult<Loan> findByUserIdPaginated(UUID userId, LoanStatus status, int page, int size);

    List<Loan> findAll();

    PageResult<Loan> findAllPaginated(LoanStatus status, int page, int size);

    long countAll();

    long countByStatus(LoanStatus status);
}
