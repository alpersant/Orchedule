package com.orchedule.scheduling.planning.audit;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

/** Utilidades de texto para los mensajes de la auditoría (español, independiente del locale del servidor). */
final class AuditFormat {

    private static final Locale SPANISH = Locale.of("es", "ES");

    private AuditFormat() {
    }

    static String day(DayOfWeek day) {
        return day.getDisplayName(TextStyle.FULL, SPANISH);
    }

    static String text(String pattern, Object... args) {
        return String.format(Locale.ROOT, pattern, args);
    }
}
