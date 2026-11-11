package com.bankloan.application.mapper;

import com.bankloan.application.dto.LoanResponse;
import com.bankloan.domain.model.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "id", expression = "java(loan.getId().toString())")
    @Mapping(target = "userId", expression = "java(loan.getUserId().toString())")
    @Mapping(target = "status", expression = "java(loan.getStatus().name())")
    @Mapping(target = "reviewedBy", expression = "java(loan.getReviewedBy() != null ? loan.getReviewedBy().toString() : null)")
    LoanResponse toResponse(Loan loan);
}
