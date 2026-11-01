package com.orchedule.scheduling.planning;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CalendarCapacityServiceTimeoutTest {
    @Test void appliesOnlyTransactionLocalStatementTimeout(){
        var db=mock(NamedParameterJdbcTemplate.class);var jdbc=mock(JdbcTemplate.class);
        when(db.getJdbcTemplate()).thenReturn(jdbc);
        var start=LocalDate.of(2030,1,7);
        Map<String,Object> row=Map.of("name","Liga","start_date",start,"end_date",start.plusWeeks(18).minusDays(1),"status","ACTIVE","active",true,"competition_status","ACTIVE");
        when(db.queryForList(anyString(),anyMap())).thenReturn(List.of(row));
        when(db.queryForObject(anyString(),anyMap(),eq(Boolean.class))).thenReturn(false);
        List<UUID> teams=new ArrayList<>();for(int i=0;i<24;i++)teams.add(UUID.randomUUID());
        var result=new CalendarCapacityService(db).check(UUID.randomUUID(),PlanningApi.Mode.SINGLE,teams,null);
        assertEquals(Integer.valueOf(23),result.requiredRounds());assertEquals(Boolean.FALSE,result.fits());
        verify(jdbc).execute("SET LOCAL statement_timeout = '1800ms'");verify(jdbc,never()).setQueryTimeout(anyInt());
    }
}
