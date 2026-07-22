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
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
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
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Constraint validation failed");
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
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.orchedule.com/errors/internal-server-error"));
        problem.setInstance(URI.create(request.getRequestURI()));

        return problem;
    }
}
