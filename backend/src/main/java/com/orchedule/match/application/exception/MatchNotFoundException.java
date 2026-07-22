package com.orchedule.match.application.exception;

import com.orchedule.shared.exception.NotFoundException;
import java.util.UUID;

public class MatchNotFoundException extends NotFoundException {
    public MatchNotFoundException(UUID matchId) { super("MATCH_NOT_FOUND", "Match not found: " + matchId); }
}
