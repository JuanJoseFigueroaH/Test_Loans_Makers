package com.bankloan.domain.model;

import java.io.Serializable;
import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) implements Serializable {

    public static <T> PageResult<T> of(List<T> content, int page, int size,
                                        long totalElements, int totalPages,
                                        boolean first, boolean last) {
        return new PageResult<>(content, page, size, totalElements, totalPages, first, last);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), 0, 0, 0, 0, true, true);
    }
}
