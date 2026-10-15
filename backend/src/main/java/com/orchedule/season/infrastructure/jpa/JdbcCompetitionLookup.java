package com.orchedule.season.infrastructure.jpa;
import com.orchedule.season.domain.CompetitionLookup;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class JdbcCompetitionLookup implements CompetitionLookup {
    private final JdbcTemplate jdbc;
    public JdbcCompetitionLookup(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public boolean exists(UUID id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
            "select exists(select 1 from competition where id = ?)", Boolean.class, id));
    }
}
