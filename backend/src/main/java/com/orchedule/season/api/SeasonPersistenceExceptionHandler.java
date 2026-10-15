package com.orchedule.season.api;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = SeasonController.class)
public class SeasonPersistenceExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integrityConflict(DataIntegrityViolationException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
            "Name already exists in this competition, or a dependency prevents the operation");
        problem.setProperty("code", "SEASON_PERSISTENCE_CONFLICT");
        return problem;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail concurrentChange(ObjectOptimisticLockingFailureException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
            "Season changed concurrently. Reload and retry");
        problem.setProperty("code", "SEASON_CONCURRENT_CHANGE");
        return problem;
    }
}
