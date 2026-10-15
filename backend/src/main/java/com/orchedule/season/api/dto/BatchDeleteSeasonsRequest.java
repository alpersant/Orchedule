package com.orchedule.season.api.dto;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
public record BatchDeleteSeasonsRequest(@NotEmpty @Size(max=100) List<@NotNull UUID> seasonIds) {}
