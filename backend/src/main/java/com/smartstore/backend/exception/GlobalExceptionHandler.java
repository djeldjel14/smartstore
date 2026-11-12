package com.smartstore.backend.exception;

import com.smartstore.backend.exception.CategoryNotFoundException;
import com.smartstore.backend.exception.DuplicateCategoryException;
import com.smartstore.backend.exception.InvalidHierarchyException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CategoryNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND", ex.getMessage(), request.getRequestURI(), List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({DuplicateCategoryException.class, InvalidHierarchyException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                "CONFLICT", ex.getMessage(), request.getRequestURI(), List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetails> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ErrorResponse.FieldErrorDetails(err.getField(), err.getCode(), err.getDefaultMessage()))
                .toList();

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR", "The request contains invalid data.", request.getRequestURI(), fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST", ex.getMessage(), request.getRequestURI(), List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}