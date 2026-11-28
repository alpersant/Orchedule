package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;
import com.orchedule.scheduling.planning.audit.AuditModel.RuleResult;

/**
 * Regla de competición verificable sobre un calendario. Patrón Strategy: el servicio de auditoría
 * recibe todas las implementaciones registradas, por lo que añadir una regla no requiere modificarlo.
 */
public interface AuditRule {

    /** Código estable que consume el cliente (no se traduce). */
    String code();

    /** Evaluación pura: no accede a base de datos ni modifica la entrada. */
    RuleResult evaluate(AuditInput input);
}
