package com.bankloan.infrastructure.adapter.out.persistence;

import com.bankloan.domain.model.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanJpaRepository extends JpaRepository<LoanJpaEntity, UUID> {

    List<LoanJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<LoanJpaEntity> findAllByOrderByCreatedAtDesc();

    Optional<LoanJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<LoanJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<LoanJpaEntity> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, LoanStatus status, Pageable pageable);

    Page<LoanJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<LoanJpaEntity> findByStatusOrderByCreatedAtDesc(LoanStatus status, Pageable pageable);

    long countByStatus(LoanStatus status);
}
