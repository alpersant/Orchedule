package com.orchedule.season.application;
import java.util.List;
import java.util.UUID;
public record BatchDeleteSeasonsResult(List<UUID> deletedIds, List<Failure> failures) {
    public record Failure(UUID seasonId, String reason) {}
}
