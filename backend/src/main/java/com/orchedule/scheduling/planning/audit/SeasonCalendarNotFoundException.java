package com.orchedule.scheduling.planning.audit;

import java.util.UUID;

/** La temporada no existe o todavía no tiene un calendario (plan) generado. */
public class SeasonCalendarNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SeasonCalendarNotFoundException(UUID seasonId) {
        super("La temporada " + seasonId + " no tiene un calendario generado");
    }
}
