package com.orchedule.scheduling.planning.partial;
import java.time.*;
import java.util.*;
public final class PlanningData {
    private PlanningData(){}
    public record Round(UUID id,UUID season,int number,int week,long version,String status,
                        LocalDate start,LocalDate end,ZoneId zone,int duration){}
    public record Game(UUID id,UUID home,UUID away,String homeName,String awayName,UUID field,Instant at){
        public PartialPlanningEngine.Game engine(){return new PartialPlanningEngine.Game(id,home,away);}
    }
    public record Preferences(Set<DayOfWeek> days,Set<LocalTime> hours,Set<DayOfWeek> firstDays,Set<LocalTime> firstHours,boolean valid){}
    public record Booking(UUID home,UUID away,UUID field,UUID venue,Instant at,int duration){}
    public record Block(String scope,UUID reference,Instant start,Instant end){}
    public record History(UUID team,Instant at,int number){}
    public record Counts(int total,int dates,int compatible,int rotation,int free){}
    public record Issue(String code,String detail,Counts counts){}
    public record Options(Map<UUID,List<PartialPlanningEngine.Slot>> candidates,Map<UUID,Issue> issues,Map<UUID,Counts> counts){}
}
