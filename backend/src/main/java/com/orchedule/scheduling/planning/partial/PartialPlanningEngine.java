package com.orchedule.scheduling.planning.partial;
import java.time.Instant;
import java.util.*;
import java.util.function.LongSupplier;

/** Pure bounded maximum-cardinality search. Fairness is a candidate-order heuristic, not a global optimum claim. */
public final class PartialPlanningEngine {
    public record Game(UUID id,UUID home,UUID away) {}
    public record Slot(UUID field,Instant at,int penalty) {}
    public record Result(Map<UUID,Slot> assigned,Set<UUID> pending,boolean searchLimited,
                         boolean cardinalityOptimal,int exploredNodes) {}
    private final LongSupplier clock;
    public PartialPlanningEngine(){this(System::nanoTime);}
    public PartialPlanningEngine(LongSupplier clock){this.clock=clock;}
    public Result solve(List<Game> games,Map<UUID,List<Slot>> candidates,Map<UUID,Slot> fixed,
                        int duration,int maxNodes,long budgetNanos){
        if(duration<1||maxNodes<1||budgetNanos<1)throw new IllegalArgumentException("Límites de planificación inválidos");
        Map<UUID,Game> lookup=new HashMap<>();Set<UUID> participants=new HashSet<>();
        for(Game g:games){
            if(g.id()==null||g.home()==null||g.away()==null||g.home().equals(g.away())||lookup.put(g.id(),g)!=null
                ||!participants.add(g.home())||!participants.add(g.away()))
                throw new IllegalArgumentException("Los cruces contienen equipos o identificadores duplicados en la jornada");
        }
        if(!lookup.keySet().containsAll(fixed.keySet()))throw new IllegalArgumentException("Asignación fija ajena a la jornada");
        List<Map.Entry<UUID,Slot>> pinned=new ArrayList<>(fixed.entrySet());
        for(int i=0;i<pinned.size();i++)for(int j=0;j<i;j++)if(conflict(pinned.get(i).getKey(),pinned.get(i).getValue(),pinned.get(j).getKey(),pinned.get(j).getValue(),lookup,duration))
            throw new IllegalArgumentException("Los horarios conservados contienen un solapamiento");
        Map<UUID,List<Slot>> options=new HashMap<>();List<UUID> order=new ArrayList<>();
        for(Game g:games)if(!fixed.containsKey(g.id())){
            List<Slot> slots=new ArrayList<>(candidates.getOrDefault(g.id(),List.of()));
            slots.sort(Comparator.comparingInt(Slot::penalty).thenComparing(Slot::at).thenComparing(Slot::field));
            slots.removeIf(s->fixed.entrySet().stream().anyMatch(p->conflict(g.id(),s,p.getKey(),p.getValue(),lookup,duration)));
            options.put(g.id(),List.copyOf(slots));order.add(g.id());
        }
        order.sort(Comparator.<UUID>comparingInt(id->options.get(id).size()).thenComparing(id->id));
        Search search=new Search(lookup,options,order,fixed,duration,maxNodes,budgetNanos,clock);
        search.visit(0,new LinkedHashMap<>(fixed));
        Set<UUID> pending=new LinkedHashSet<>(lookup.keySet());pending.removeAll(search.best.keySet());
        return new Result(Map.copyOf(search.best),Set.copyOf(pending),search.limited&&!search.optimal,
            search.optimal||!search.limited,search.nodes);
    }
    private static boolean conflict(UUID a,Slot x,UUID b,Slot y,Map<UUID,Game> games,int minutes){
        boolean overlaps=x.at().isBefore(y.at().plusSeconds(minutes*60L))&&y.at().isBefore(x.at().plusSeconds(minutes*60L));
        if(!overlaps)return false;
        Game ga=games.get(a),gb=games.get(b);
        return x.field().equals(y.field())||ga.home().equals(gb.home())||ga.home().equals(gb.away())||ga.away().equals(gb.home())||ga.away().equals(gb.away());
    }
    private static final class Search {
        final Map<UUID,Game> games;final Map<UUID,List<Slot>> options;final List<UUID> order;
        final int duration,maxNodes,target;final long start,budget;final LongSupplier clock;
        Map<UUID,Slot> best;int nodes;boolean limited,optimal;
        Search(Map<UUID,Game> games,Map<UUID,List<Slot>> options,List<UUID> order,Map<UUID,Slot> fixed,
               int duration,int maxNodes,long budget,LongSupplier clock){
            this.games=games;this.options=options;this.order=order;this.duration=duration;this.maxNodes=maxNodes;this.budget=budget;this.clock=clock;
            this.start=clock.getAsLong();this.best=new LinkedHashMap<>(fixed);
            this.target=fixed.size()+(int)order.stream().filter(id->!options.get(id).isEmpty()).count();
        }
        void visit(int position,Map<UUID,Slot> current){
            if(current.size()>best.size())best=new LinkedHashMap<>(current);
            if(best.size()==target){optimal=true;return;}
            if(optimal||limited)return;
            if(++nodes>maxNodes||clock.getAsLong()-start>=budget){limited=true;return;}
            if(position==order.size())return;
            int remaining=0;for(int i=position;i<order.size();i++)if(!options.get(order.get(i)).isEmpty())remaining++;
            if(current.size()+remaining<=best.size())return;
            UUID id=order.get(position);
            for(Slot slot:options.get(id)){
                if(current.entrySet().stream().anyMatch(p->conflict(id,slot,p.getKey(),p.getValue(),games,duration)))continue;
                current.put(id,slot);visit(position+1,current);current.remove(id);
                if(optimal||limited)return;
            }
            // Skipping an unschedulable match is a normal branch, not an exception.
            visit(position+1,current);
        }
    }
}
