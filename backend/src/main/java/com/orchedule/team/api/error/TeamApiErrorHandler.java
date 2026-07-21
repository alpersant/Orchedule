package com.orchedule.team.api.error;

import com.orchedule.identity.api.error.ApiError;
import com.orchedule.team.application.exception.InvalidTeamPreferenceException;
import com.orchedule.team.application.exception.TeamNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class TeamApiErrorHandler {

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<ApiError> handleTeamNotFound(TeamNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                "TEAM_NOT_FOUND",
                ex.getMessage(),
                OffsetDateTime.now()
        ));
    }

    @ExceptionHandler(InvalidTeamPreferenceException.class)
    public ResponseEntity<ApiError> handleInvalidPreference(InvalidTeamPreferenceException ex) {
        return ResponseEntity.badRequest().body(new ApiError(
                "INVALID_TEAM_PREFERENCE",
                ex.getMessage(),
                OffsetDateTime.now()
        ));
    }
}