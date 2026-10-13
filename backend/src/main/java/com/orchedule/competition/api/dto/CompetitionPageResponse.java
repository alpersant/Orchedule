package com.orchedule.competition.api.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record CompetitionPageResponse(
        List<CompetitionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static CompetitionPageResponse from(Page<CompetitionResponse> page) {
        return new CompetitionPageResponse(
                page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
