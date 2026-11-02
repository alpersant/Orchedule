package com.orchedule.scheduling.planning.partial;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import static com.orchedule.scheduling.planning.partial.PlanningData.*;

@Service
public class PartialRoundService {
    private final PartialPlanningRepository repo;private final PartialSlotResolver resolver;private final SeasonContextPort seasons;
    private final int maxNodes;private final long budgetNanos;
    public PartialRoundService(PartialPlanningRepository repo,PartialSlotResolver resolver,SeasonContextPort seasons,
        @Value("${orchedule.planning.partial.max-nodes:100000}") int maxNodes,
        @Value("${orchedule.planning.partial.search-ms:1000}") long searchMs){
        if(maxNodes<1||maxNodes>1000000||searchMs<1||searchMs>5000)throw new IllegalArgumentException("Límites de planificación parcial inválidos");
        this.repo=repo;this.resolver=resolver;this.seasons=seasons;this.maxNodes=maxNodes;this.budgetNanos=searchMs*1_000_000;
    }
    private static ResponseStatusException conflict(String text){return new ResponseStatusException(HttpStatus.CONFLICT,text);}
    @Transactional(timeout=8)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public void execute(UUID season,int number,String action,long version){
        if(!Set.of("schedule","resolve","regenerate","confirm","cancel").contains(action))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Acción desconocida");
        repo.lock(season);seasons.assertSeasonIsSchedulable(season);Round r=repo.round(season,number);
        if(r.version()!=version)throw conflict("La jornada ha cambiado. Actualiza antes de continuar.");
        if(repo.laterConfirmed(r))throw conflict("Hay jornadas posteriores confirmadas. Cancélalas en orden inverso antes de modificar esta.");
        List<Game> games=repo.games(r);if(games.isEmpty())throw conflict("La jornada no tiene enfrentamientos");
        if(action.equals("cancel")){
            if(r.status().equals("CANCELLED"))throw conflict("La jornada ya está cancelada");
            repo.cancelPublished(r);repo.updateStatus(r,"CANCELLED",false,false);repo.audit(r,"CANCEL");return;
        }
        if(r.status().equals("CONFIRMED"))throw conflict("Cancela la jornada confirmada antes de modificar sus horarios");
        if(repo.activeLegacy(r))throw conflict("Hay reservas individuales anteriores activas en esta semana. Revísalas y cancélalas explícitamente antes de reutilizarla.");
        if(action.equals("confirm")){
            if(!r.status().equals("DRAFT"))throw conflict("Solo se confirma un borrador");
            long pending=games.stream().filter(g->g.at()==null||g.field()==null).count();
            if(pending>0)throw conflict("Quedan "+pending+" partidos pendientes. Puedes guardar y revisar el borrador, pero no confirmar la jornada completa.");
            if(repo.earlierIncomplete(r))throw conflict("Puedes preparar esta jornada, pero antes de publicarla debes completar y confirmar las anteriores para revalidar la rotación.");
            Options options=resolver.resolve(r,games);Map<UUID,PartialPlanningEngine.Slot> fixed=new LinkedHashMap<>();
            for(Game g:games){
                var slot=matching(options.candidates().getOrDefault(g.id(),List.of()),g);
                if(slot==null)throw conflict("El horario de "+g.homeName()+" — "+g.awayName()+" ya no es válido. Usa Resolver o revisar horarios para obtener el diagnóstico y conservar las asignaciones que sigan siendo válidas.");
                fixed.put(g.id(),slot);
            }
            // Also validates that conserved assignments do not overlap one another.
            new PartialPlanningEngine().solve(games.stream().map(Game::engine).toList(),options.candidates(),fixed,r.duration(),maxNodes,budgetNanos);
            repo.publish(r);repo.updateStatus(r,"CONFIRMED",false,false);repo.audit(r,"CONFIRM");return;
        }
        if(action.equals("resolve")&&!r.status().equals("DRAFT"))throw conflict("Resolver pendientes requiere un borrador. Prepara primero la jornada.");
        if(action.equals("schedule")&&!r.status().equals("UNSCHEDULED"))throw conflict("Usa Resolver pendientes para conservar el borrador o Recalcular todo para sustituir horarios.");
        Options options=resolver.resolve(r,games);
        Map<UUID,List<PartialPlanningEngine.Slot>> candidates=new LinkedHashMap<>(options.candidates());
        Map<UUID,PartialPlanningEngine.Slot> fixed=new LinkedHashMap<>();Map<UUID,Issue> invalid=new HashMap<>();
        boolean reorganize=action.equals("regenerate");
        if(!reorganize)for(Game g:games)if(g.at()!=null&&g.field()!=null){
            var slot=matching(candidates.getOrDefault(g.id(),List.of()),g);
            if(slot!=null)fixed.put(g.id(),slot);
            else{
                // Do not silently move an existing reservation after a configuration change.
                candidates.put(g.id(),List.of());
                invalid.put(g.id(),new Issue("PREVIOUS_ASSIGNMENT_INVALID","El horario guardado dejó de ser válido y ahora queda pendiente. No se ha movido a otro horario silenciosamente. Revisa sus condiciones y vuelve a resolver pendientes o recalcula el borrador completo.",options.counts().get(g.id())));
            }
        }
        var result=new PartialPlanningEngine().solve(games.stream().map(Game::engine).toList(),candidates,fixed,r.duration(),maxNodes,budgetNanos);
        Map<UUID,Issue> issues=new LinkedHashMap<>();
        for(UUID id:result.pending()){
            Issue issue=invalid.get(id);if(issue==null)issue=options.issues().get(id);
            if(issue==null)issue=new Issue(result.searchLimited()?"SEARCH_LIMIT_REACHED":"JOINT_CAPACITY_CONFLICT",
                result.searchLimited()?"No se encontró horario dentro del límite de búsqueda. Se conserva la propuesta válida encontrada; esto no demuestra que el partido sea imposible.":
                "No se asignó una franja en la propuesta conjunta: compite por recursos con otros partidos. Puede ser necesario ampliar disponibilidad o reorganizar el borrador.",options.counts().get(id));
            issues.put(id,issue);
        }
        repo.saveDraft(r,games,result.assigned(),issues,repo.earlierIncomplete(r),result.searchLimited());
        repo.audit(r,action.toUpperCase(Locale.ROOT));
    }
    static PartialPlanningEngine.Slot matching(List<PartialPlanningEngine.Slot> slots,Game game){
        if(game.at()==null||game.field()==null)return null;
        return slots.stream().filter(s->s.field().equals(game.field())&&s.at().equals(game.at())).findFirst().orElse(null);
    }
}
