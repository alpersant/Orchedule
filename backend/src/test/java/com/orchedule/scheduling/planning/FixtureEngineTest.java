package com.orchedule.scheduling.planning;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FixtureEngineTest {
    @Test void singleLeaguePropertiesForAllSupportedSizes(){
        for(int count=2;count<=64;count++){
            List<UUID> teams=new ArrayList<>();for(int i=0;i<count;i++) teams.add(new UUID(0,i+1));
            var rounds=FixtureEngine.league(teams,false);
            assertEquals(count%2==0?count-1:count,rounds.size());
            Set<String> pairs=new HashSet<>();Map<UUID,Integer> byes=new HashMap<>();
            for(var round:rounds){
                Set<UUID> used=new HashSet<>();assertEquals(count/2,round.games().size());
                for(var game:round.games()){
                    assertTrue(used.add(game.home()));assertTrue(used.add(game.away()));
                    String key=game.home().compareTo(game.away())<0?game.home()+"/"+game.away():game.away()+"/"+game.home();
                    assertTrue(pairs.add(key));
                }
                if(count%2==1){assertNotNull(round.bye());assertFalse(used.contains(round.bye()));byes.merge(round.bye(),1,Integer::sum);}
                else assertNull(round.bye());
            }
            assertEquals(count*(count-1)/2,pairs.size());
            if(count%2==1) for(UUID id:teams) assertEquals(Integer.valueOf(1),byes.get(id));
        }
    }
    @Test void returnLegReversesEveryPairAndPreservesBye(){
        for(int n=2;n<=64;n++){
            List<UUID> teams=new ArrayList<>();for(int i=0;i<n;i++) teams.add(new UUID(0,i+1));
            var rounds=FixtureEngine.league(teams,true);int half=rounds.size()/2;
            for(int i=0;i<half;i++){
                assertEquals(rounds.get(i).bye(),rounds.get(i+half).bye());
                for(int j=0;j<rounds.get(i).games().size();j++){
                    assertEquals(rounds.get(i).games().get(j).home(),rounds.get(i+half).games().get(j).away());
                    assertEquals(rounds.get(i).games().get(j).away(),rounds.get(i+half).games().get(j).home());
                }
            }
        }
    }
    @Test void weekOneContainsStartAndHandlesYearBoundary(){
        assertEquals(LocalDate.of(2026,10,26),FixtureEngine.monday(LocalDate.of(2026,10,31),1));
        assertEquals(LocalDate.of(2027,1,4),FixtureEngine.monday(LocalDate.of(2026,12,31),2));
    }
    @Test void recoversFromGreedyDeadEnd(){
        UUID f=new UUID(0,1),a=new UUID(0,2),b=new UUID(0,3);
        Instant t=Instant.parse("2026-11-02T18:00:00Z");
        var s1=new FixtureEngine.Slot(f,t,0);var s2=new FixtureEngine.Slot(f,t.plusSeconds(3600),1);
        var candidates=new LinkedHashMap<UUID,List<FixtureEngine.Slot>>();
        candidates.put(a,List.of(s1,s2));candidates.put(b,List.of(s1));
        var result=FixtureEngine.allocate(candidates,60,100);
        assertEquals(s2,result.get(a));assertEquals(s1,result.get(b));
    }
    @Test void impossibleScheduleDoesNotReturnPartialAllocation(){
        UUID f=new UUID(0,1);var slot=new FixtureEngine.Slot(f,Instant.EPOCH,0);
        assertThrows(IllegalArgumentException.class,()->FixtureEngine.allocate(Map.of(new UUID(0,2),List.of(slot),new UUID(0,3),List.of(slot)),60,100));
    }
    @Test void adjacentGamesDoNotOverlap(){
        assertFalse(FixtureEngine.overlaps(Instant.EPOCH,60,Instant.EPOCH.plusSeconds(3600),60));
        assertTrue(FixtureEngine.overlaps(Instant.EPOCH,90,Instant.EPOCH.plusSeconds(3600),60));
    }
    @Test void searchHasAnExplicitLimit(){
        UUID id=new UUID(0,1);
        assertThrows(IllegalArgumentException.class,()->FixtureEngine.allocate(Map.of(id,List.of(new FixtureEngine.Slot(id,Instant.EPOCH,0))),60,0));
    }
}
