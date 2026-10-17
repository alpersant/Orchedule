package com.orchedule.field.infrastructure.integration;

import com.orchedule.field.application.exception.InvalidFieldException;
import com.orchedule.field.application.exception.WeeklyAvailabilityConflictException;
import com.orchedule.field.application.port.WeeklyAvailabilityPolicy;
import com.orchedule.field.domain.FieldValidator;
import com.orchedule.season.application.exception.SeasonNotFoundException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Parameterized ACL queries; writes must be called inside a transaction. */
@Component
public class JdbcWeeklyAvailabilityPolicy implements WeeklyAvailabilityPolicy {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcWeeklyAvailabilityPolicy(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void validateRead(UUID seasonId, Integer weekNumber) {
        validateInput(seasonId, weekNumber);
        Boolean exists = jdbc.queryForObject(
                "select exists(select 1 from season where id = :id)",
                Map.of("id", seasonId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw new SeasonNotFoundException(seasonId);
        }
    }

    @Override
    public void lockAndValidateWrite(UUID seasonId, int weekNumber, List<UUID> enabledFieldIds) {
        validateInput(seasonId, weekNumber);
        if (enabledFieldIds == null || enabledFieldIds.size() > 500
                || enabledFieldIds.stream().anyMatch(java.util.Objects::isNull)
                || new HashSet<>(enabledFieldIds).size() != enabledFieldIds.size()) {
            throw new InvalidFieldException("Field ids must be unique, non-null, and at most 500");
        }

        // Serializes weekly availability writers for the same season.
        List<String> statuses = jdbc.queryForList(
                "select status from season where id = :id for update",
                Map.of("id", seasonId), String.class);
        if (statuses.isEmpty()) {
            throw new SeasonNotFoundException(seasonId);
        }
        if ("CLOSED".equals(statuses.getFirst())) {
            throw new WeeklyAvailabilityConflictException("A closed season cannot be edited");
        }
        Boolean generated = jdbc.queryForObject("""
                select exists(select 1 from schedule_round
                  where season_id = :id and week_number = :week
                    and status in ('DRAFT', 'CONFIRMED'))
                """, Map.of("id", seasonId, "week", weekNumber), Boolean.class);
        if (Boolean.TRUE.equals(generated)) {
            throw new WeeklyAvailabilityConflictException(
                    "An existing round prevents changing weekly availability; review the calendar");
        }
        if (!enabledFieldIds.isEmpty()) {
            Long eligible = jdbc.queryForObject("""
                    select count(*) from field f join venue v on v.id = f.venue_id
                    where f.id in (:ids) and f.status = 'ACTIVE'
                      and v.active = true and v.status = 'ACTIVE'
                    """, Map.of("ids", enabledFieldIds), Long.class);
            if (eligible == null || eligible.longValue() != enabledFieldIds.size()) {
                throw new InvalidFieldException(
                        "Every selected field and its venue must exist and be active");
            }
        }
    }

    private void validateInput(UUID seasonId, Integer weekNumber) {
        if (seasonId == null) {
            throw new InvalidFieldException("Season id is required");
        }
        if (weekNumber != null) {
            FieldValidator.validateWeekNumber(weekNumber);
        }
    }
}
