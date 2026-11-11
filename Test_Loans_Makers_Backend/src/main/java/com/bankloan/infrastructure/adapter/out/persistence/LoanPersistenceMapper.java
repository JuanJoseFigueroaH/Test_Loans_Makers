package com.bankloan.infrastructure.adapter.out.persistence;

import com.bankloan.domain.model.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanPersistenceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "termMonths", source = "termMonths")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "reviewedAt", source = "reviewedAt")
    @Mapping(target = "reviewedBy", source = "reviewedBy")
    @Mapping(target = "rejectionReason", source = "rejectionReason")
    @Mapping(target = "version", source = "version")
    LoanJpaEntity toJpaEntity(Loan loan);

    default Loan toDomain(LoanJpaEntity entity) {
        return Loan.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getAmount(),
                entity.getTermMonths(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getReviewedAt(),
                entity.getReviewedBy(),
                entity.getRejectionReason(),
                entity.getVersion()
        );
    }
}
