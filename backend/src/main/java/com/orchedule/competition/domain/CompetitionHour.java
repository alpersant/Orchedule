package com.orchedule.competition.domain;

public enum CompetitionHour {
    H18("18:00"),
    H19("19:00"),
    H20("20:00"),
    H21("21:00"),
    H22("22:00");

    private final String label;

    CompetitionHour(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
