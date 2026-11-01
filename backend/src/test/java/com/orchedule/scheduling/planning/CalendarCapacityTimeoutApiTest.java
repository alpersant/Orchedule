package com.orchedule.scheduling.planning;
import com.orchedule.season.application.ExtendSeasonEndDateService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CalendarCapacityTimeoutApiTest {
    @Test void timedOutCheckReturnsSafeRetryableError(){
        var api=new CalendarCapacityApi(mock(CalendarCapacityService.class),mock(ExtendSeasonEndDateService.class));
        var response=api.timeout(new QueryTimeoutException("internal database detail"));
        assertEquals(503,response.getStatusCode().value());assertNotNull(response.getBody());
        assertFalse(response.getBody().get("message").contains("internal database detail"));
    }
}
