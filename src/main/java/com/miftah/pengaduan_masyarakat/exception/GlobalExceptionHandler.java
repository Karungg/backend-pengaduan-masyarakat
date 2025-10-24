package com.miftah.pengaduan_masyarakat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.miftah.pengaduan_masyarakat.dto.GenericResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        GenericResponse<String> response = GenericResponse.badRequest(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        GenericResponse<String> response = GenericResponse.notFound(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<GenericResponse<String>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.warn("Authentication failed - user not found: {}", ex.getMessage());
        GenericResponse<String> response = GenericResponse.notFound(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Map<String, List<String>>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        GenericResponse<Map<String, List<String>>> response = GenericResponse.badRequest(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<String>> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        GenericResponse<String> response = GenericResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Terjadi kesalahan internal pada server.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // JWT Exception

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<GenericResponse<String>> handleExpiredJwtException(ExpiredJwtException ex) {
        log.warn("JWT token has expired: {}", ex.getMessage());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.UNAUTHORIZED,
                "Token JWT telah kedaluwarsa.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ SignatureException.class, MalformedJwtException.class })
    public ResponseEntity<GenericResponse<String>> handleInvalidJwtSignatureOrFormatException(JwtException ex) {
        log.warn("Invalid JWT token: {}", ex.getMessage());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.UNAUTHORIZED, "Token JWT tidak valid.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<GenericResponse<String>> handleJwtException(JwtException ex) {
        log.error("Error processing JWT token: {}", ex.getMessage());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.UNAUTHORIZED,
                "Terjadi masalah saat memproses token.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GenericResponse<String>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.UNAUTHORIZED,
                "Autentikasi gagal: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.FORBIDDEN, "Akses ditolak.");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
