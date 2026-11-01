package com.orchedule.shared.api;

import com.orchedule.shared.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "No tienes permisos para esta operación");
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrity(org.springframework.dao.DataIntegrityViolationException ex,
                                        HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        String sqlState = "unknown";
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable cause = ex; cause != null && seen.add(cause); cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null) {
                sqlState = sql.getSQLState();
            }
        }
        // Full technical cause stays in server logs. No body/token/SQL detail in the HTTP response.
        log.error("Data integrity conflict errorId={} method={} path={} sqlState={}",
                errorId, request.getMethod(), request.getRequestURI(), sqlState, ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "No se ha podido guardar el calendario o recurso por un conflicto de datos. "
                + "El detalle técnico se ha registrado en el servidor.");
        problem.setTitle("Conflicto de datos");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "DATA_INTEGRITY_CONFLICT");
        problem.setProperty("errorId", errorId);
        return problem;
    }


    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail handleApplicationException(
            ApplicationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
        problem.setTitle(ex.getHttpStatus().getReasonPhrase());
        problem.setType(URI.create("https://api.orchedule.com/errors/" + ex.getErrorCode().toLowerCase()));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", ex.getErrorCode());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Revisa los datos enviados");
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://api.orchedule.com/errors/request-validation-failed"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(
                "errors",
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(fieldError -> new ApiValidationError(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        ))
                        .toList()
        );

        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Los datos no cumplen las reglas de validación");
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://api.orchedule.com/errors/constraint-validation-failed"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(
                "errors",
                ex.getConstraintViolations()
                        .stream()
                        .map(violation -> new ApiValidationError(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        ))
                        .toList()
        );

        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected API failure on {}", request.getRequestURI(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo completar la operación por un error interno");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.orchedule.com/errors/internal-server-error"));
        problem.setInstance(URI.create(request.getRequestURI()));

        return problem;
    }
}

