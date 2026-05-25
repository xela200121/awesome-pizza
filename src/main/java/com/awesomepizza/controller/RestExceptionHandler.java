package com.awesomepizza.controller;

import com.awesomepizza.exception.OrderStatusException;
import com.awesomepizza.exception.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request", buildValidationDetail(exception));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage() {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request", "Request body is missing or malformed");
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleOrderNotFound(OrderNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "Order not found", exception.getMessage());
    }

    @ExceptionHandler(OrderStatusException.class)
    public ResponseEntity<ProblemDetail> handleOrderConflict(OrderStatusException exception) {
        return buildResponse(HttpStatus.CONFLICT, "Order conflict", exception.getMessage());
    }

    private ResponseEntity<ProblemDetail> buildResponse(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        return ResponseEntity.status(status).body(problemDetail);
    }

    private String buildValidationDetail(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));

        if (detail.isBlank()) {
            return "Request validation failed";
        }

        return detail;
    }
}
