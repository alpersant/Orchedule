package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.partial.PlanningData.Preferences;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

/** Modelo inmutable de la auditoría: entrada, resultado de reglas y métricas de equidad. */
public final class AuditModel {

    /** Máximo de incumplimientos detallados por regla; el total real siempre se informa. */
    public static final int MAX_VIOLATIONS_PER_RULE = 50;

    static final Comparator<ScheduledGame> CHRONOLOGICAL = Comparator
            .comparing(ScheduledGame::at)
            .thenComparingInt(ScheduledGame::roundNumber)
            .thenComparing(ScheduledGame::id);

    private AuditModel() {
    }

    /** Partido con horario asignado (confirmado, o borrador si se solicita). */
    public record ScheduledGame(UUID id, int roundNumber, String roundStatus, UUID home, UUID away,
                                String homeName, String awayName, UUID field, Instant at) {
        public ScheduledGame {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(home, "home");
            Objects.requireNonNull(away, "away");
            Objects.requireNonNull(at, "at");
        }

        public boolean involves(UUID team) {
            return home.equals(team) || away.equals(team);
        }
    }

    public record TeamRule(int minRestHours, int maxMatchesPerWeek) {
        public TeamRule {
            if (minRestHours < 0 || maxMatchesPerWeek < 1) {
                throw new IllegalArgumentException("Regla de equipo no válida");
            }
        }
    }

    /** Todo lo necesario para auditar una temporada, ya leído y sin dependencias de infraestructura. */
    public record AuditInput(UUID seasonId, ZoneId zone, int durationMinutes, TeamRule defaultRule,
                             Map<UUID, TeamRule> teamRules, Map<UUID, String> participants,
                             Map<UUID, Preferences> preferences, List<ScheduledGame> games,
                             int pendingGames, boolean includesDraft) {
        public AuditInput {
            Objects.requireNonNull(seasonId, "seasonId");
            Objects.requireNonNull(zone, "zone");
            Objects.requireNonNull(defaultRule, "defaultRule");
            if (durationMinutes <= 0) {
                throw new IllegalArgumentException("La duración debe ser positiva");
            }
            if (pendingGames < 0) {
                throw new IllegalArgumentException("Partidos pendientes no válido");
            }
            teamRules = Map.copyOf(teamRules);
            participants = Collections.unmodifiableMap(new LinkedHashMap<>(participants));
            preferences = Map.copyOf(preferences);
            games = List.copyOf(games);
        }

        public TeamRule ruleFor(UUID team) {
            return teamRules.getOrDefault(team, defaultRule);
        }

        public String nameOf(UUID team) {
            return participants.getOrDefault(team, team.toString());
        }

        /** Partidos de cada equipo en orden cronológico. Coste O(n log n); se calcula por llamada. */
        public Map<UUID, List<ScheduledGame>> timelinesByTeam() {
            Map<UUID, List<ScheduledGame>> result = new TreeMap<>();
            for (ScheduledGame game : games) {
                result.computeIfAbsent(game.home(), k -> new ArrayList<>()).add(game);
                result.computeIfAbsent(game.away(), k -> new ArrayList<>()).add(game);
            }
            result.values().forEach(list -> list.sort(CHRONOLOGICAL));
            return result;
        }
    }

    public record Violation(UUID teamId, UUID gameId, Integer roundNumber, String message) {
    }

    public enum RuleStatus { PASS, FAIL, NOT_APPLICABLE }

    public record RuleResult(String code, RuleStatus status, int checked, int violationCount,
                             List<Violation> violations, List<String> notes) {

        public static RuleResult of(String code, int checked, List<Violation> all, List<String> notes) {
            RuleStatus status = checked == 0
                    ? RuleStatus.NOT_APPLICABLE
                    : all.isEmpty() ? RuleStatus.PASS : RuleStatus.FAIL;
            List<Violation> shown = all.size() > MAX_VIOLATIONS_PER_RULE
                    ? List.copyOf(all.subList(0, MAX_VIOLATIONS_PER_RULE))
                    : List.copyOf(all);
            return new RuleResult(code, status, checked, all.size(), shown, List.copyOf(notes));
        }
    }

    /** Métricas de reparto de un equipo. {@code score} es nulo si el equipo aún no tiene partidos. */
    public record TeamFairness(UUID teamId, String teamName, int games,
                               Map<String, Integer> gamesByDay, Map<String, Integer> gamesByHour,
                               int maxDayStreak, int maxHourStreak, int streakViolations,
                               double dayEvenness, double hourEvenness,
                               Double firstOptionShare, Double score) {
        public TeamFairness {
            gamesByDay = Collections.unmodifiableMap(new LinkedHashMap<>(gamesByDay));
            gamesByHour = Collections.unmodifiableMap(new LinkedHashMap<>(gamesByHour));
        }
    }

    public enum FairnessRating { EXCELLENT, GOOD, ACCEPTABLE, NEEDS_ATTENTION, NOT_AVAILABLE }

    public record LeagueFairness(int teamsEvaluated, Double meanScore, Double minScore, Double maxScore,
                                 Double gap, Double standardDeviation, FairnessRating rating) {
    }

    public record AuditReport(UUID seasonId, Instant generatedAt, String zoneId, boolean includesDraft,
                              int evaluatedGames, int pendingGames, boolean compliant,
                              List<RuleResult> rules, LeagueFairness league, List<TeamFairness> teams) {
        public AuditReport {
            rules = List.copyOf(rules);
            teams = List.copyOf(teams);
        }
    }
}
