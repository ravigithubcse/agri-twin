package com.agritwin.farmtwin.exception;

import com.agritwin.farmtwin.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FarmTwinNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFarmTwinNotFound(
            FarmTwinNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(FarmTwinAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleFarmTwinAlreadyExists(
            FarmTwinAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(HttpStatus.CONFLICT.value(), "CONFLICT", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(LandParcelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLandParcelNotFound(
            LandParcelNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(CropHistoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCropHistoryNotFound(
            CropHistoryNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedToResourceException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedToResourceException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.of(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR",
                        "Request validation failed", request.getRequestURI(), details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR",
                        "An unexpected error occurred", request.getRequestURI()));
    }
}
