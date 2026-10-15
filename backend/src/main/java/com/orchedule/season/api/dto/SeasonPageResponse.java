package com.orchedule.season.api.dto;
import java.util.List;
public record SeasonPageResponse(List<SeasonResponse> content, int page, int size,
        long totalElements, int totalPages, boolean last) {}
