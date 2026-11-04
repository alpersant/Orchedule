package com.orchedule.scheduling.planning.partial;
import com.orchedule.scheduling.application.port.*;
import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.planning.FixtureEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.*;
import static com.orchedule.scheduling.planning.partial.PlanningData.*;

@Component
public class PartialSlotResolver {
    private final PartialPlanningRepository repo;private final SeasonContextPort seasons;private final FieldAvailabilityPort fields;private final Clock clock;
    @Autowired
    public PartialSlotResolver(PartialPlanningRepository repo,SeasonContextPort seasons,FieldAvailabilityPort fields){this(repo,seasons,fields,Clock.systemUTC());}
    PartialSlotResolver(PartialPlanningRepository repo,SeasonContextPort seasons,FieldAvailabilityPort fields,Clock clock){this.repo=repo;this.seasons=seasons;this.fields=fields;this.clock=clock;}
    public Options resolve(Round round,List<Game> games){
        UUID competition=seasons.getCompetitionIdForSeason(round.season());
        repo.lockFieldInputs(round);
        List<FieldScheduleSlot> slots;
        try{slots=fields.getSchedulingContext(competition,round.season(),round.week()).slots();}
        catch(InvalidScheduleException expectedAvailabilityProblem){slots=List.of();}
        Map<UUID,List<PartialPlanningEngine.Slot>> candidates=new LinkedHashMap<>();Map<UUID,Issue> issues=new HashMap<>();Map<UUID,Counts> counts=new HashMap<>();
        if(slots.isEmpty()){
            for(Game game:games){Counts c=new Counts(0,0,0,0,0);candidates.put(game.id(),List.of());counts.put(game.id(),c);issues.put(game.id(),diagnose(c,false));}
            return new Options(candidates,issues,counts);
        }
        Set<UUID> teamIds=new HashSet<>(),fieldIds=new HashSet<>();for(Game g:games){teamIds.add(g.home());teamIds.add(g.away());}slots.forEach(s->fieldIds.add(s.fieldId()));
        var venues=repo.venues(fieldIds);Set<UUID> venueIds=new HashSet<>(venues.values());
        var profiles=repo.preferences(competition,teamIds);
        Map<UUID,List<ZonedDateTime>> history=new HashMap<>();for(History h:repo.history(round))history.computeIfAbsent(h.team(),k->new ArrayList<>()).add(h.at().atZone(round.zone()));
        LocalDate monday=FixtureEngine.monday(round.start(),round.week());
        Instant from=monday.minusDays(1).atStartOfDay(round.zone()).toInstant(),to=monday.plusWeeks(1).atStartOfDay(round.zone()).toInstant();
        var bookings=repo.bookings(round,from,to,teamIds,fieldIds,venueIds);var blocks=repo.blocks(from,to);var proposals=repo.proposals(round);
        Set<String> opening=new HashSet<>();for(var s:slots)opening.add(s.fieldId()+"/"+s.day()+"/"+s.hour());
        Instant now=clock.instant();
        for(Game game:games){
            Preferences home=profiles.get(game.home()),away=profiles.get(game.away());boolean missing=home==null||away==null||!home.valid()||!away.valid();
            int dates=0,compatible=0,rotation=0,free=0;List<PartialPlanningEngine.Slot> possible=new ArrayList<>();
            if(!missing)for(var slot:slots){
                LocalDate date=monday.plusDays(slot.day().getValue()-1L);if(date.isBefore(round.start())||date.isAfter(round.end()))continue;
                LocalDateTime local=date.atTime(slot.hour());var offsets=round.zone().getRules().getValidOffsets(local);if(offsets.size()!=1)continue;
                Instant at=local.toInstant(offsets.getFirst());
                if(at.isBefore(now)||at.plusSeconds(round.duration()*60L).isAfter(date.plusDays(1).atStartOfDay(round.zone()).toInstant()))continue;
                boolean covered=true;for(int minute=0;minute<round.duration();minute+=60)if(!opening.contains(slot.fieldId()+"/"+slot.day()+"/"+slot.hour().plusMinutes(minute))){covered=false;break;}
                if(!covered)continue;dates++;
                if(!allowed(home,slot)||!allowed(away,slot))continue;compatible++;
                if(overused(history.get(game.home()),slot)||overused(history.get(game.away()),slot))continue;rotation++;
                boolean occupied=false;
                for(Block block:blocks){
                    boolean affected=block.scope().equals("TEAM")&&(block.reference().equals(game.home())||block.reference().equals(game.away()))
                        ||block.scope().equals("VENUE")&&Objects.equals(block.reference(),venues.get(slot.fieldId()));
                    if(affected&&at.isBefore(block.end())&&block.start().isBefore(at.plusSeconds(round.duration()*60L))){occupied=true;break;}
                }
                if(occupied)continue;
                for(Booking b:bookings){
                    boolean resource=Objects.equals(b.field(),slot.fieldId())||b.field()==null&&Objects.equals(b.venue(),venues.get(slot.fieldId()))
                        ||List.of(game.home(),game.away()).contains(b.home())||List.of(game.home(),game.away()).contains(b.away());
                    if(resource&&FixtureEngine.overlaps(at,round.duration(),b.at(),b.duration())){occupied=true;break;}
                }
                if(!occupied)for(Game proposal:proposals){
                    boolean affected=!proposal.id().equals(game.id())&&(proposal.home().equals(game.home())||proposal.home().equals(game.away())||proposal.away().equals(game.home())||proposal.away().equals(game.away()));
                    if(affected&&FixtureEngine.overlaps(at,round.duration(),proposal.at(),round.duration())){occupied=true;break;}
                }
                if(occupied)continue;free++;
                int penalty=preferenceCost(home,slot)+preferenceCost(away,slot);
                for(UUID id:List.of(game.home(),game.away()))for(var h:history.getOrDefault(id,List.of())){
                    if(h.getDayOfWeek()==slot.day())penalty+=2;if(h.toLocalTime().equals(slot.hour()))penalty+=2;
                }
                possible.add(new PartialPlanningEngine.Slot(slot.fieldId(),at,penalty));
            }
            Counts c=new Counts(slots.size(),dates,compatible,rotation,free);counts.put(game.id(),c);candidates.put(game.id(),List.copyOf(possible));
            if(possible.isEmpty())issues.put(game.id(),diagnose(c,missing));
        }
        return new Options(candidates,issues,counts);
    }
    static boolean allowed(Preferences p,FieldScheduleSlot s){return p.days().contains(s.day())&&p.hours().contains(s.hour());}
    static int preferenceCost(Preferences p,FieldScheduleSlot s){return (p.firstDays().contains(s.day())?0:10)+(p.firstHours().contains(s.hour())?0:10);}
    static boolean overused(List<ZonedDateTime> h,FieldScheduleSlot s){
        if(h==null||h.size()<2)return false;
        return h.get(0).getDayOfWeek()==s.day()&&h.get(1).getDayOfWeek()==s.day()
            ||h.get(0).toLocalTime().equals(s.hour())&&h.get(1).toLocalTime().equals(s.hour());
    }
    static Issue diagnose(Counts c,boolean missing){
        if(c.total()==0)return new Issue("NO_FIELDS_ENABLED","No hay campos y franjas habilitados en esta semana. Revisa Campos y horarios.",c);
        if(missing)return new Issue("MISSING_PREFERENCES","Faltan preferencias válidas o hay un participante inactivo. Revisa las preferencias de ambos equipos.",c);
        if(c.dates()==0)return new Issue("NO_VALID_DATES","No quedan franjas válidas dentro de las fechas de esta semana. Revisa el inicio, el final, las fechas pasadas y la apertura de los campos.",c);
        if(c.compatible()==0)return new Issue("NO_SHARED_AVAILABILITY","Ninguna franja de los campos habilitados es compatible con los días y horas admitidos por ambos equipos. Las exclusiones se mantienen.",c);
        if(c.rotation()==0)return new Issue("ROTATION_LIMIT","Las franjas compatibles repetirían el mismo día u hora por tercera vez en el histórico disponible. No se ha relajado la regla de rotación.",c);
        return new Issue("RESOURCE_CONFLICT","Las franjas compatibles están ocupadas o bloqueadas para un equipo o instalación. Revisa disponibilidad y reservas.",c);
    }
}

