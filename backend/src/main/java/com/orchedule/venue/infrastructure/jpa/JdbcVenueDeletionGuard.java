package com.orchedule.venue.infrastructure.jpa;

import com.orchedule.venue.application.port.VenueDeletionGuard;
import com.orchedule.venue.application.exception.VenueNotFoundException;
import com.orchedule.venue.application.exception.VenueInUseException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
public class JdbcVenueDeletionGuard implements VenueDeletionGuard {
    private final NamedParameterJdbcTemplate jdbc;
    public JdbcVenueDeletionGuard(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void lockAndAssertDeletable(UUID venueId) {
        var parameters = Map.of("id", venueId);
        var rows = jdbc.queryForList("select id from venue where id = :id for update", parameters);
        if (rows.isEmpty()) { throw new VenueNotFoundException(venueId); }
        Boolean used = jdbc.queryForObject("""
                select exists(select 1 from field where venue_id = :id)
                    or exists(select 1 from match_game where venue_id = :id)
                """, parameters, Boolean.class);
        if (Boolean.TRUE.equals(used)) { throw new VenueInUseException(); }
    }
}
