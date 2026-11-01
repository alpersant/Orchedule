package com.orchedule.season.application;
import com.orchedule.season.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ExtendSeasonEndDateServiceTest {
    @Test void extensionPreservesNameStartCompetitionAndStatus(){
        var db=mock(NamedParameterJdbcTemplate.class);var jdbc=mock(JdbcTemplate.class);var repo=mock(SeasonRepository.class);
        UUID id=UUID.randomUUID(),competition=UUID.randomUUID();var start=LocalDate.of(2030,1,7);var end=start.plusWeeks(18).minusDays(1);var newEnd=end.plusWeeks(5);
        var now=OffsetDateTime.now();
        var old=new Season(id,competition,"Liga",start,end,SeasonStatus.ACTIVE,true,now,now);
        var updated=new Season(id,competition,"Liga",start,newEnd,SeasonStatus.ACTIVE,true,now,now);
        when(db.getJdbcTemplate()).thenReturn(jdbc);when(db.queryForList(anyString(),anyMap())).thenReturn(List.of(Map.of("id",id)));
        when(repo.findById(id)).thenReturn(Optional.of(old));when(repo.update(id,"Liga",start,newEnd,SeasonStatus.ACTIVE,true)).thenReturn(updated);
        var response=new ExtendSeasonEndDateService(db,repo).extend(id,start,end,newEnd);
        assertEquals(newEnd,response.endDate());assertEquals(competition,response.competitionId());assertEquals(start,response.startDate());
        verify(repo).update(id,"Liga",start,newEnd,SeasonStatus.ACTIVE,true);
    }
    @Test void staleDatesDoNotOverwriteAnotherSessionsChanges(){
        var db=mock(NamedParameterJdbcTemplate.class);var jdbc=mock(JdbcTemplate.class);var repo=mock(SeasonRepository.class);
        UUID id=UUID.randomUUID();var start=LocalDate.of(2030,1,7);var end=start.plusWeeks(18);var now=OffsetDateTime.now();
        when(db.getJdbcTemplate()).thenReturn(jdbc);when(db.queryForList(anyString(),anyMap())).thenReturn(List.of(Map.of("id",id)));
        when(repo.findById(id)).thenReturn(Optional.of(new Season(id,UUID.randomUUID(),"Liga",start,end,SeasonStatus.ACTIVE,true,now,now)));
        assertThrows(ResponseStatusException.class,()->new ExtendSeasonEndDateService(db,repo).extend(id,start,end.minusDays(1),end.plusWeeks(5)));
        verify(repo,never()).update(any(),anyString(),any(),any(),any(),anyBoolean());
    }
}
