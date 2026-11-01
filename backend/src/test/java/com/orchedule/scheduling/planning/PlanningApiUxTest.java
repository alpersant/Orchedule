package com.orchedule.scheduling.planning;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PlanningApiUxTest {
    @Test void acceptsTwentyFourTeamsWithoutTimezoneOrDuration(){
        var service=mock(PlanningService.class);UUID id=UUID.randomUUID();
        when(service.read(id)).thenReturn(Map.of());
        List<UUID> teams=new ArrayList<>();for(int i=0;i<24;i++)teams.add(UUID.randomUUID());
        new PlanningApi(service,"Europe/Madrid",60).configure(id,new PlanningApi.Configure(PlanningApi.Mode.SINGLE,teams,null,null,null));
        var capture=ArgumentCaptor.forClass(PlanningApi.Configure.class);
        verify(service).configure(eq(id),capture.capture());
        assertEquals(24,capture.getValue().teamIds().size());
        assertEquals("Europe/Madrid",capture.getValue().zoneId());assertEquals(Integer.valueOf(60),capture.getValue().durationMinutes());
    }
    @Test void ignoresClientTimezoneAndUsesServerDefault(){
        var service=mock(PlanningService.class);UUID id=UUID.randomUUID();when(service.read(id)).thenReturn(Map.of());
        new PlanningApi(service,"UTC",60).configure(id,new PlanningApi.Configure(PlanningApi.Mode.SINGLE,List.of(UUID.randomUUID(),UUID.randomUUID()),"Pacific/Auckland",null,null));
        var capture=ArgumentCaptor.forClass(PlanningApi.Configure.class);verify(service).configure(eq(id),capture.capture());
        assertEquals("UTC",capture.getValue().zoneId());
    }
    @Test void invalidDeploymentConfigurationFailsFast(){
        var service=mock(PlanningService.class);
        assertThrows(java.time.DateTimeException.class,()->new PlanningApi(service,"Invalid/Zone",60));
        assertThrows(IllegalArgumentException.class,()->new PlanningApi(service,"UTC",0));
    }
}
