package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditInput;

import java.util.UUID;

/** Puerto de lectura de la auditoría. Mantiene el dominio de reglas y equidad libre de SQL. */
public interface AuditDataPort {

    /**
     * Lee todo lo necesario para auditar una temporada.
     *
     * @param includeDraft si es {@code true} incluye también las jornadas en borrador
     * @throws SeasonCalendarNotFoundException si la temporada no tiene calendario
     */
    AuditInput load(UUID seasonId, boolean includeDraft);
}
