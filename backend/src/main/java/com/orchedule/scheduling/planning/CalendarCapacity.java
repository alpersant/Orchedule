package com.orchedule.scheduling.planning;
import java.time.*;
import java.time.temporal.ChronoUnit;

/** Date capacity only: this is not a proof of compatible fields or preferences. */
public final class CalendarCapacity {
    private CalendarCapacity() {}
    public record Result(int availableWeeks,Integer requiredRounds,Integer missingWeeks,
                         Boolean fits,LocalDate earliestEndDate,LocalDate suggestedEndDate) {}
    public static int roundCount(PlanningApi.Mode mode,int teams,Integer manualRounds){
        if(teams<2||teams>64)throw new IllegalArgumentException("Selecciona entre 2 y 64 equipos");
        if(mode==PlanningApi.Mode.MANUAL){
            if(manualRounds==null||manualRounds==0)return 0;
            if(manualRounds<0||manualRounds>126)throw new IllegalArgumentException("Número de jornadas manuales inválido");
            return manualRounds;
        }
        int first=teams%2==0?teams-1:teams;
        return mode==PlanningApi.Mode.DOUBLE?2*first:first;
    }
    public static Result evaluate(LocalDate start,LocalDate end,Integer required){
        if(start==null||end==null||end.isBefore(start))throw new IllegalArgumentException("El periodo de temporada no es válido");
        // Same anchor as the final generation guard in PlanningService.
        LocalDate monday=FixtureEngine.monday(start,1);
        int weeks=Math.toIntExact(ChronoUnit.DAYS.between(monday,end)/7+1);
        if(required==null||required==0)return new Result(weeks,null,null,null,null,null);
        if(required<0)throw new IllegalArgumentException("Número de jornadas inválido");
        LocalDate lastMonday=FixtureEngine.monday(start,required);
        return new Result(weeks,required,Math.max(0,required-weeks),!lastMonday.isAfter(end),lastMonday,lastMonday.plusDays(6));
    }
}
