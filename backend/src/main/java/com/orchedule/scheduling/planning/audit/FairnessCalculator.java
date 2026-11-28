package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.FairnessRating;
import com.orchedule.scheduling.planning.audit.AuditModel.LeagueFairness;
import com.orchedule.scheduling.planning.audit.AuditModel.ScheduledGame;
import com.orchedule.scheduling.planning.audit.AuditModel.TeamFairness;
import com.orchedule.scheduling.planning.partial.PlanningData.Preferences;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Calcula la puntuación de equidad (0-100) de cada equipo y el resumen de la liga.
 *
 * <p>Puntuación de un equipo = media ponderada de cuatro componentes en [0, 1]:</p>
 * <ul>
 *   <li>Reparto por día (30 %): entropía normalizada sobre los días permitidos y jugados.</li>
 *   <li>Reparto por hora (30 %): igual, sobre las horas.</li>
 *   <li>Rachas (25 %): penaliza las ventanas de tres jornadas con el mismo día u hora.</li>
 *   <li>Primeras opciones (15 %): proporción de partidos en día y hora de primera opción.
 *       Se omite, renormalizando los pesos, si el equipo no tiene preferencias válidas.</li>
 * </ul>
 *
 * <p>La entropía se normaliza por {@code ln(min(partidos, opciones))}: con pocos partidos solo
 * se exige tanta variedad como sea físicamente posible. Los pesos son una propuesta de producto
 * y están aislados en constantes para ajustarlos sin tocar el algoritmo.</p>
 */
@Component
public class FairnessCalculator {

    static final double DAY_WEIGHT = 0.30;
    static final double HOUR_WEIGHT = 0.30;
    static final double STREAK_WEIGHT = 0.25;
    static final double FIRST_OPTION_WEIGHT = 0.15;

    static final double EXCELLENT_FROM = 85.0;
    static final double GOOD_FROM = 70.0;
    static final double ACCEPTABLE_FROM = 50.0;

    /** Un resultado por participante, ordenado por nombre. */
    public List<TeamFairness> teams(AuditInput input) {
        Map<UUID, List<ScheduledGame>> timelines = input.timelinesByTeam();

        return input.participants().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> evaluateTeam(input, entry.getKey(), entry.getValue(),
                        timelines.getOrDefault(entry.getKey(), List.of())))
                .toList();
    }

    public LeagueFairness league(List<TeamFairness> teams) {
        List<Double> scores = teams.stream().map(TeamFairness::score).filter(s -> s != null).toList();
        if (scores.isEmpty()) {
            return new LeagueFairness(0, null, null, null, null, null, FairnessRating.NOT_AVAILABLE);
        }

        double mean = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double min = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double variance = scores.stream().mapToDouble(s -> (s - mean) * (s - mean)).average().orElse(0);

        return new LeagueFairness(scores.size(), round1(mean), round1(min), round1(max),
                round1(max - min), round1(Math.sqrt(variance)), rating(min));
    }

    static FairnessRating rating(double worstTeamScore) {
        if (worstTeamScore >= EXCELLENT_FROM) {
            return FairnessRating.EXCELLENT;
        }
        if (worstTeamScore >= GOOD_FROM) {
            return FairnessRating.GOOD;
        }
        if (worstTeamScore >= ACCEPTABLE_FROM) {
            return FairnessRating.ACCEPTABLE;
        }
        return FairnessRating.NEEDS_ATTENTION;
    }

    private TeamFairness evaluateTeam(AuditInput input, UUID teamId, String name, List<ScheduledGame> chronological) {
        int games = chronological.size();
        if (games == 0) {
            return new TeamFairness(teamId, name, 0, Map.of(), Map.of(), 0, 0, 0, 1.0, 1.0, null, null);
        }

        ZoneId zone = input.zone();
        List<ScheduledGame> byRound = new ArrayList<>(chronological);
        byRound.sort(Comparator.comparingInt(ScheduledGame::roundNumber).thenComparing(ScheduledGame::at));

        List<DayOfWeek> days = new ArrayList<>();
        List<LocalTime> hours = new ArrayList<>();
        Map<DayOfWeek, Integer> byDay = new EnumMap<>(DayOfWeek.class);
        Map<LocalTime, Integer> byHour = new TreeMap<>();
        for (ScheduledGame game : byRound) {
            ZonedDateTime local = game.at().atZone(zone);
            days.add(local.getDayOfWeek());
            hours.add(local.toLocalTime());
            byDay.merge(local.getDayOfWeek(), 1, Integer::sum);
            byHour.merge(local.toLocalTime(), 1, Integer::sum);
        }

        Preferences prefs = input.preferences().get(teamId);
        boolean hasPreferences = prefs != null && prefs.valid();

        Set<DayOfWeek> dayOptions = new HashSet<>(byDay.keySet());
        Set<LocalTime> hourOptions = new HashSet<>(byHour.keySet());
        if (hasPreferences) {
            dayOptions.addAll(prefs.days());
            hourOptions.addAll(prefs.hours());
        }

        double dayEvenness = evenness(dayOptions, byDay, games);
        double hourEvenness = evenness(hourOptions, byHour, games);

        int streakViolations = 0;
        for (int i = 0; i + 2 < games; i++) {
            boolean sameDay = days.get(i).equals(days.get(i + 1)) && days.get(i).equals(days.get(i + 2));
            boolean sameHour = hours.get(i).equals(hours.get(i + 1)) && hours.get(i).equals(hours.get(i + 2));
            if (sameDay || sameHour) {
                streakViolations++;
            }
        }
        double streakScore = games < 3 ? 1.0 : 1.0 - Math.min(1.0, streakViolations / (games - 2.0));

        Double firstOptionShare = null;
        if (hasPreferences) {
            int first = 0;
            for (int i = 0; i < games; i++) {
                if (prefs.firstDays().contains(days.get(i)) && prefs.firstHours().contains(hours.get(i))) {
                    first++;
                }
            }
            firstOptionShare = round3((double) first / games);
        }

        double weighted = DAY_WEIGHT * dayEvenness + HOUR_WEIGHT * hourEvenness + STREAK_WEIGHT * streakScore;
        double totalWeight = DAY_WEIGHT + HOUR_WEIGHT + STREAK_WEIGHT;
        if (firstOptionShare != null) {
            weighted += FIRST_OPTION_WEIGHT * firstOptionShare;
            totalWeight += FIRST_OPTION_WEIGHT;
        }
        double score = round1(100.0 * weighted / totalWeight);

        Map<String, Integer> gamesByDay = new LinkedHashMap<>();
        byDay.forEach((day, count) -> gamesByDay.put(day.name(), count));
        Map<String, Integer> gamesByHour = new LinkedHashMap<>();
        byHour.forEach((hour, count) -> gamesByHour.put(hour.toString(), count));

        return new TeamFairness(teamId, name, games, gamesByDay, gamesByHour,
                longestRun(days), longestRun(hours), streakViolations,
                round3(dayEvenness), round3(hourEvenness), firstOptionShare, score);
    }

    /** Entropía de Shannon normalizada: 1 = reparto perfecto, 0 = todo en una sola opción. */
    private static <T> double evenness(Set<T> options, Map<T, Integer> counts, int games) {
        int k = options.size();
        if (games < 2 || k <= 1) {
            return 1.0;
        }

        double entropy = 0.0;
        for (T option : options) {
            int count = counts.getOrDefault(option, 0);
            if (count > 0) {
                double p = (double) count / games;
                entropy -= p * Math.log(p);
            }
        }
        double maxEntropy = Math.log(Math.min(games, k));
        return Math.max(0.0, Math.min(1.0, entropy / maxEntropy));
    }

    private static <T> int longestRun(List<T> values) {
        int best = 0;
        int current = 0;
        for (int i = 0; i < values.size(); i++) {
            current = i > 0 && values.get(i).equals(values.get(i - 1)) ? current + 1 : 1;
            best = Math.max(best, current);
        }
        return best;
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
