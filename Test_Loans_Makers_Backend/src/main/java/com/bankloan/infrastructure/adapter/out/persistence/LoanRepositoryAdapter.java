package com.bankloan.infrastructure.adapter.out.persistence;

import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.PageResult;
import com.bankloan.domain.port.out.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LoanRepositoryAdapter implements LoanRepository {

    private final LoanJpaRepository jpaRepository;
    private final LoanPersistenceMapper mapper;

    public LoanRepositoryAdapter(LoanJpaRepository jpaRepository, LoanPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Loan save(Loan loan) {
        LoanJpaEntity entity = mapper.toJpaEntity(loan);
        LoanJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Loan saveAndFlush(Loan loan) {
        LoanJpaEntity entity = mapper.toJpaEntity(loan);
        LoanJpaEntity saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Loan> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Loan> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public PageResult<Loan> findByUserIdPaginated(UUID userId, LoanStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanJpaEntity> entityPage;
        if (status != null) {
            entityPage = jpaRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            entityPage = jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return toPageResult(entityPage);
    }

    @Override
    public List<Loan> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Loan> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Loan> findAllPaginated(LoanStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoanJpaEntity> entityPage;
        if (status != null) {
            entityPage = jpaRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            entityPage = jpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return toPageResult(entityPage);
    }

    @Override
    public long countAll() {
        return jpaRepository.count();
    }

    @Override
    public long countByStatus(LoanStatus status) {
        return jpaRepository.countByStatus(status);
    }

    private PageResult<Loan> toPageResult(Page<LoanJpaEntity> entityPage) {
        List<Loan> content = entityPage.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        return PageResult.of(
                content,
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.isFirst(),
                entityPage.isLast()
        );
    }
}
