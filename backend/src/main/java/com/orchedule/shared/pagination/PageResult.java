package com.orchedule.shared.pagination;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, int page, int size, long totalElements,
                            int totalPages, boolean last) {
    public PageResult { content = List.copyOf(content); }
    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = content.stream().map(mapper).collect(java.util.stream.Collectors.toList());
        return new PageResult<>(mapped, page, size, totalElements, totalPages, last);
    }
}
