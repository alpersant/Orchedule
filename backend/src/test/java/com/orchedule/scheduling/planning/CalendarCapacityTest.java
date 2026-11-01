package com.orchedule.scheduling.planning;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class CalendarCapacityTest {
    @Test void twentyFourTeamsNeedTwentyThreeOrFortySixRounds(){
        assertEquals(23,CalendarCapacity.roundCount(PlanningApi.Mode.SINGLE,24,null));
        assertEquals(46,CalendarCapacity.roundCount(PlanningApi.Mode.DOUBLE,24,null));
    }
    @Test void oddParticipantsHaveAnExtraRoundForByes(){
        assertEquals(5,CalendarCapacity.roundCount(PlanningApi.Mode.SINGLE,5,null));
        assertEquals(10,CalendarCapacity.roundCount(PlanningApi.Mode.DOUBLE,5,null));
    }
    @Test void countAgreesWithActualPairingEngineForEverySupportedSize(){
        for(int n=2;n<=64;n++){
            List<UUID> teams=new ArrayList<>();for(int i=0;i<n;i++)teams.add(new UUID(0,i+1));
            assertEquals(FixtureEngine.league(teams,false).size(),CalendarCapacity.roundCount(PlanningApi.Mode.SINGLE,n,null));
            assertEquals(FixtureEngine.league(teams,true).size(),CalendarCapacity.roundCount(PlanningApi.Mode.DOUBLE,n,null));
        }
    }
    @Test void partialWeeksAreIncludedWithoutMovingTheStartDate(){
        var r=CalendarCapacity.evaluate(LocalDate.of(2026,11,4),LocalDate.of(2026,11,15),2);
        assertEquals(2,r.availableWeeks());assertEquals(Boolean.TRUE,r.fits());
        assertEquals(LocalDate.of(2026,11,9),r.earliestEndDate());
        assertEquals(LocalDate.of(2026,11,15),r.suggestedEndDate());
    }
    @Test void insufficientDatesReportShortfallAndSuggestedEnd(){
        var start=LocalDate.of(2030,1,7);
        var result=CalendarCapacity.evaluate(start,start.plusWeeks(18).minusDays(1),23);
        assertEquals(18,result.availableWeeks());assertEquals(Integer.valueOf(5),result.missingWeeks());
        assertEquals(Boolean.FALSE,result.fits());assertEquals(start.plusWeeks(22).plusDays(6),result.suggestedEndDate());
    }
    @Test void weekCountHandlesYearAndLeapYearBoundaries(){
        assertEquals(2,CalendarCapacity.evaluate(LocalDate.of(2026,12,31),LocalDate.of(2027,1,10),2).availableWeeks());
        assertEquals(2,CalendarCapacity.evaluate(LocalDate.of(2028,2,29),LocalDate.of(2028,3,12),2).availableWeeks());
    }
    @Test void unknownManualRoundsAreNotClaimedToFit(){
        var result=CalendarCapacity.evaluate(LocalDate.of(2030,1,7),LocalDate.of(2030,2,1),0);
        assertNull(result.fits());assertNull(result.requiredRounds());
    }
    @Test void preflightFitMatchesExistingFinalGuard(){
        var start=LocalDate.of(2030,1,9);
        for(int required=1;required<=126;required++)for(int days=0;days<150;days++){
            var end=start.plusDays(days);
            assertTrue(CalendarCapacity.evaluate(start,end,required).fits()==!FixtureEngine.monday(start,required).isAfter(end));
        }
    }
}
