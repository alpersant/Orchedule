package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.ScheduledGame;
import com.orchedule.scheduling.planning.audit.AuditModel.TeamRule;
import com.orchedule.scheduling.planning.partial.PlanningData.Preferences;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Construcción compacta de entradas de auditoría para los tests unitarios. */
final class AuditTestSupport {

    static final ZoneId ZONE = ZoneId.of("Europe/Madrid");
    static final UUID TEAM_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    static final UUID TEAM_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    static final UUID FIELD = UUID.fromString("00000000-0000-0000-0000-0000000000f1");

    // Madrid en enero es UTC+1: martes 19:00 local = 18:00Z.
    static final String TUE_19 = "2035-01-02T18:00:00Z";
    static final String WED_19 = "2035-01-03T18:00:00Z";
    static final String THU_19 = "2035-01-04T18:00:00Z";
    static final String FRI_19 = "2035-01-05T18:00:00Z";

    private AuditTestSupport() {
    }

    static ScheduledGame game(int round, UUID home, UUID away, String at) {
        return game(round, home, away, FIELD, at);
    }

    static ScheduledGame game(int round, UUID home, UUID away, UUID field, String at) {
        return new ScheduledGame(UUID.randomUUID(), round, "CONFIRMED", home, away,
                "Equipo " + home.toString().substring(34), "Equipo " + away.toString().substring(34),
                field, Instant.parse(at));
    }

    static AuditInput input(List<ScheduledGame> games) {
        return input(games, Map.of(), Map.of());
    }

    static AuditInput input(List<ScheduledGame> games, Map<UUID, TeamRule> teamRules,
                            Map<UUID, Preferences> preferences) {
        Map<UUID, String> participants = new LinkedHashMap<>();
        for (ScheduledGame game : games) {
            participants.putIfAbsent(game.home(), game.homeName());
            participants.putIfAbsent(game.away(), game.awayName());
        }
        return new AuditInput(UUID.randomUUID(), ZONE, 60, new TeamRule(48, 2), teamRules,
                participants, preferences, games, 0, false);
    }

    static Preferences preferences(Set<DayOfWeek> days, Set<LocalTime> hours) {
        return new Preferences(days, hours, days, hours, true);
    }
}
