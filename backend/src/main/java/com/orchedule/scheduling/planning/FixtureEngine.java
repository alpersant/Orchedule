package com.orchedule.scheduling.planning;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/** Pure and deterministic pairing/constraint operations, independent of Spring. */
public final class FixtureEngine {
    private FixtureEngine() {}
    public record Pair(UUID home, UUID away) {}
    public record Round(List<Pair> games, UUID bye) {}
    public record Slot(UUID field, Instant at, int penalty) {}
    public static List<Round> league(List<UUID> teams, boolean doubleLeg) {
        if(teams.size()<2 || new HashSet<>(teams).size()!=teams.size())
            throw new IllegalArgumentException("Selecciona dos o más equipos distintos");
        List<UUID> ring=new ArrayList<>(teams);
        if(ring.size()%2!=0) ring.add(null);
        List<Round> result=new ArrayList<>(); int n=ring.size();
        for(int r=0;r<n-1;r++) {
            List<Pair> games=new ArrayList<>(); UUID bye=null;
            for(int i=0;i<n/2;i++) {
                UUID a=ring.get(i),b=ring.get(n-1-i);
                if(a==null || b==null) bye=a==null?b:a;
                else games.add((r+i)%2==0?new Pair(a,b):new Pair(b,a));
            }
            result.add(new Round(List.copyOf(games),bye));
            ring.add(1,ring.remove(n-1));
        }
        if(doubleLeg) for(Round r:List.copyOf(result)) result.add(new Round(
            r.games().stream().map(p->new Pair(p.away(),p.home())).toList(),r.bye()));
        return List.copyOf(result);
    }
    public static LocalDate monday(LocalDate start,int week) {
        if(week<1) throw new IllegalArgumentException("Semana inválida");
        return start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(week-1L);
    }
    public static boolean overlaps(Instant a,int duration,Instant b,int otherDuration) {
        return a.isBefore(b.plusSeconds(otherDuration*60L)) && b.isBefore(a.plusSeconds(duration*60L));
    }
    public static Map<UUID,Slot> allocate(Map<UUID,List<Slot>> candidates,int duration,int limit) {
        List<UUID> ids=new ArrayList<>(candidates.keySet());
        ids.sort(Comparator.comparingInt(id->candidates.get(id).size()));
        Map<UUID,Slot> result=new LinkedHashMap<>();
        if(!search(0,ids,candidates,result,duration,new int[]{limit}))
            throw new IllegalArgumentException("No se ha encontrado una planificación completa. Revisa campos y restricciones.");
        return result;
    }
    private static boolean search(int i,List<UUID> ids,Map<UUID,List<Slot>> options,
        Map<UUID,Slot> result,int duration,int[] budget) {
        if(i==ids.size()) return true;
        if(--budget[0]<0) throw new IllegalArgumentException("Límite de búsqueda alcanzado; amplía disponibilidad");
        UUID id=ids.get(i);
        for(Slot slot:options.get(id).stream().sorted(Comparator.comparingInt(Slot::penalty)
            .thenComparing(Slot::at).thenComparing(Slot::field)).toList()) {
            if(result.values().stream().anyMatch(other->other.field().equals(slot.field()) && overlaps(slot.at(),duration,other.at(),duration))) continue;
            result.put(id,slot);
            if(search(i+1,ids,options,result,duration,budget)) return true;
            result.remove(id);
        }
        return false;
    }
}
