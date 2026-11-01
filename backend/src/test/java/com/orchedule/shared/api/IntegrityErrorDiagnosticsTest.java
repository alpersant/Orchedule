package com.orchedule.shared.api;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class IntegrityErrorDiagnosticsTest {
    @Test void returnsReferenceWithoutExposingDatabaseDetails(){
        var request=new MockHttpServletRequest("POST","/api/v1/planning/seasons/example");
        var ex=new DataIntegrityViolationException("private database information",new SQLException("private constraint detail","23503"));
        var result=new GlobalExceptionHandler().handleIntegrity(ex,request);
        assertEquals(409,result.getStatus());
        assertNotNull(result.getProperties());assertNotNull(result.getProperties().get("errorId"));
        assertDoesNotThrow(()->UUID.fromString(result.getProperties().get("errorId").toString()));
        assertEquals("DATA_INTEGRITY_CONFLICT",result.getProperties().get("errorCode"));
        assertFalse(result.getDetail().contains("private"));
    }
}
