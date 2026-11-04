package com.orchedule.scheduling.planning.partial;

import com.orchedule.scheduling.application.port.SeasonContextPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static com.orchedule.scheduling.planning.partial.PlanningData.*;

class GameAssignmentServiceTest {
    NamedParameterJdbcTemplate db=mock(NamedParameterJdbcTemplate.class);
    PartialPlanningRepository repo=mock(PartialPlanningRepository.class);
    PartialSlotResolver resolver=mock(PartialSlotResolver.class);
    SeasonContextPort seasons=mock(SeasonContextPort.class);
    GameAssignmentService service=new GameAssignmentService(db,repo,resolver,seasons);
    UUID season=UUID.randomUUID(),id=UUID.randomUUID(),roundId=UUID.randomUUID();
    Map<String,Object> row=new HashMap<>();
    @BeforeEach void setup(){
        row.put("round_number",1);row.put("confirmation_status","PROVISIONAL");
        when(db.queryForList(anyString(),org.mockito.ArgumentMatchers.<String,Object>anyMap())).thenReturn(List.of(row));
        var start=LocalDate.now(ZoneOffset.UTC).plusWeeks(2).with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        when(repo.round(season,1)).thenReturn(new Round(roundId,season,1,1,3,"DRAFT",start,start.plusWeeks(4),ZoneId.of("Europe/Madrid"),60));
        when(repo.games(any())).thenReturn(List.of(new Game(id,UUID.randomUUID(),UUID.randomUUID(),"Local","Visitante",null,null)));
    }
    @Test void rejectsStaleRevisionBeforeAssignment(){
        var ex=assertThrows(ResponseStatusException.class,()->service.assign(season,id,2,"2030-01-01T18:00:00",null));
        assertEquals(409,ex.getStatusCode().value());verifyNoInteractions(resolver);
    }
    @Test void rejectsEditingConfirmedGame(){
        row.put("confirmation_status","CONFIRMED");
        var ex=assertThrows(ResponseStatusException.class,()->service.assign(season,id,3,"2030-01-01T18:00:00",null));
        assertEquals(409,ex.getStatusCode().value());verifyNoInteractions(resolver);
    }
    @Test void rejectsPastDate(){
        var ex=assertThrows(ResponseStatusException.class,()->service.assign(season,id,3,"2000-01-01T18:00:00",null));
        assertEquals(409,ex.getStatusCode().value());verifyNoInteractions(resolver);
    }
    @Test void cannotConfirmWithoutField(){
        var ex=assertThrows(ResponseStatusException.class,()->service.confirm(season,id,3L));
        assertEquals(409,ex.getStatusCode().value());verifyNoInteractions(resolver);
    }
    @Test void rejectsUnknownGame(){
        when(db.queryForList(anyString(),org.mockito.ArgumentMatchers.<String,Object>anyMap())).thenReturn(List.of());
        var ex=assertThrows(ResponseStatusException.class,()->service.assign(season,id,3,"2030-01-01T18:00:00",null));
        assertEquals(404,ex.getStatusCode().value());
    }
}
