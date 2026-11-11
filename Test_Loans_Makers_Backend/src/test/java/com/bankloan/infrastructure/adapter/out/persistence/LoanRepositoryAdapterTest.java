package com.bankloan.infrastructure.adapter.out.persistence;

import com.bankloan.domain.model.Loan;
import com.bankloan.domain.model.LoanStatus;
import com.bankloan.domain.model.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanRepositoryAdapterTest {

    @Mock private LoanJpaRepository jpaRepository;
    @Mock private LoanPersistenceMapper mapper;

    @InjectMocks private LoanRepositoryAdapter adapter;

    private final UUID loanId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private LoanJpaEntity entity;
    private Loan domainLoan;

    @BeforeEach
    void setUp() {
        entity = new LoanJpaEntity();
        entity.setId(loanId);
        entity.setUserId(userId);
        entity.setAmount(BigDecimal.valueOf(5000000));
        entity.setTermMonths(12);
        entity.setStatus(LoanStatus.PENDING);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setVersion(0L);

        domainLoan = Loan.reconstitute(loanId, userId, BigDecimal.valueOf(5000000), 12,
                LoanStatus.PENDING, Instant.now(), Instant.now(), null, null, null, 0L);
    }

    @Test
    void save_shouldMapAndPersist() {
        when(mapper.toJpaEntity(domainLoan)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        Loan result = adapter.save(domainLoan);
        assertThat(result.getId()).isEqualTo(loanId);
        verify(jpaRepository).save(entity);
    }

    @Test
    void saveAndFlush_shouldMapAndFlush() {
        when(mapper.toJpaEntity(domainLoan)).thenReturn(entity);
        when(jpaRepository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        Loan result = adapter.saveAndFlush(domainLoan);
        assertThat(result.getId()).isEqualTo(loanId);
        verify(jpaRepository).saveAndFlush(entity);
    }

    @Test
    void findById_shouldReturnLoan() {
        when(jpaRepository.findById(loanId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        Optional<Loan> result = adapter.findById(loanId);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(loanId);
    }

    @Test
    void findById_shouldReturnEmpty() {
        when(jpaRepository.findById(loanId)).thenReturn(Optional.empty());

        Optional<Loan> result = adapter.findById(loanId);
        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndUserId_shouldReturnLoan() {
        when(jpaRepository.findByIdAndUserId(loanId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        Optional<Loan> result = adapter.findByIdAndUserId(loanId, userId);
        assertThat(result).isPresent();
    }

    @Test
    void findByUserIdPaginated_withStatus_shouldFilterByStatus() {
        Page<LoanJpaEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByUserIdAndStatusOrderByCreatedAtDesc(eq(userId), eq(LoanStatus.PENDING), any())).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        PageResult<Loan> result = adapter.findByUserIdPaginated(userId, LoanStatus.PENDING, 0, 10);
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void findByUserIdPaginated_withoutStatus_shouldReturnAll() {
        Page<LoanJpaEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any())).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        PageResult<Loan> result = adapter.findByUserIdPaginated(userId, null, 0, 10);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    void findByUserId_shouldReturnList() {
        when(jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        List<Loan> result = adapter.findByUserId(userId);
        assertThat(result).hasSize(1);
    }

    @Test
    void findAll_shouldReturnList() {
        when(jpaRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        List<Loan> result = adapter.findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    void findAllPaginated_withStatus_shouldFilter() {
        Page<LoanJpaEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findByStatusOrderByCreatedAtDesc(eq(LoanStatus.APPROVED), any())).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        PageResult<Loan> result = adapter.findAllPaginated(LoanStatus.APPROVED, 0, 10);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    void findAllPaginated_withoutStatus_shouldReturnAll() {
        Page<LoanJpaEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findAllByOrderByCreatedAtDesc(any())).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domainLoan);

        PageResult<Loan> result = adapter.findAllPaginated(null, 0, 10);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    void countAll_shouldReturnCount() {
        when(jpaRepository.count()).thenReturn(5L);
        assertThat(adapter.countAll()).isEqualTo(5);
    }

    @Test
    void countByStatus_shouldReturnCount() {
        when(jpaRepository.countByStatus(LoanStatus.PENDING)).thenReturn(3L);
        assertThat(adapter.countByStatus(LoanStatus.PENDING)).isEqualTo(3);
    }
}
