package com.orchedule.scheduling.planning.partial;
import com.orchedule.scheduling.planning.PlanningService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.util.*;

@Service
public class PlanningReadFacade {
    private final PlanningService core;private final PartialPlanningRepository repo;
    public PlanningReadFacade(PlanningService core,PartialPlanningRepository repo){this.core=core;this.repo=repo;}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ,timeout=5)
    @PreAuthorize("isAuthenticated()")
    public Map<String,Object> read(UUID season){
        Map<String,Object> result=new LinkedHashMap<>(core.read(season));
        Map<String,Map<String,Object>> issues=new HashMap<>();
        for(var row:repo.issues(season))issues.put(row.get("game_id").toString(),row);
        List<Map<String,Object>> games=copyRows(result.get("games"));Map<String,Integer> assigned=new HashMap<>(),pending=new HashMap<>();
        for(var game:games){
            if(game.get("scheduled_at")==null && game.get("proposed_at")!=null){
                var configurations=copyRows(result.get("configuration"));
                java.time.ZoneId zone=java.time.ZoneId.of(configurations.getFirst().get("zone_id").toString());
                game.put("local_start",PartialPlanningRepository.instant(game.get("proposed_at")).atZone(zone).toLocalDateTime().toString());
            }
            String round=game.get("round_id").toString();boolean scheduled=game.get("field_id")!=null&&game.get("scheduled_at")!=null;
            (scheduled?assigned:pending).merge(round,1,Integer::sum);
            game.put("planning_issue",scheduled?null:issues.get(game.get("id").toString()));
        }
        List<Map<String,Object>> rounds=copyRows(result.get("rounds"));rounds.sort(Comparator.comparingInt(r->com.orchedule.shared.jdbc.JdbcValues.requiredInt(r, "round_number")));
        boolean earlierIncomplete=false;
        for(var round:rounds){
            String id=round.get("id").toString();int a=assigned.getOrDefault(id,0),p=pending.getOrDefault(id,0);
            round.put("assigned_count",a);round.put("pending_count",p);
            round.put("history_provisional",earlierIncomplete||Boolean.TRUE.equals(round.get("history_provisional")));
            round.put("earlier_incomplete",earlierIncomplete);
            round.put("planning_status",p==0?"COMPLETE":a==0?"EMPTY":"PARTIAL");
            if(!"CONFIRMED".equals(round.get("status")))earlierIncomplete=true;
        }
        result.put("games",games);result.put("rounds",rounds);return result;
    }
    @SuppressWarnings("unchecked")
    private static List<Map<String,Object>> copyRows(Object value){
        List<Map<String,Object>> rows=new ArrayList<>();for(Object row:(List<?>)value)rows.add(new LinkedHashMap<>((Map<String,Object>)row));return rows;
    }
}

