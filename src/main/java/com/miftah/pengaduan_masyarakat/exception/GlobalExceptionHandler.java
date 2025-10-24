package com.miftah.pengaduan_masyarakat.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());

        String message = messageSource.getMessage("error.illegal.argument", new Object[] { ex.getMessage() },
                ex.getMessage(), LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.badRequest(message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        String message = messageSource.getMessage("error.resource.notfound", new Object[] { ex.getMessage() },
                ex.getMessage(), LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.notFound(message);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<GenericResponse<String>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.warn("Authentication failed - user not found: {}", ex.getMessage());

        String message = messageSource.getMessage("error.auth.usernotfound", new Object[] { ex.getMessage() },
                ex.getMessage(), LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.notFound(message);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Map<String, List<String>>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new HashMap<>();
        Locale currentLocale = LocaleContextHolder.getLocale();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();

            String errorMessage = messageSource.getMessage(error, currentLocale);
            errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        GenericResponse<Map<String, List<String>>> response = GenericResponse.badRequest(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // JWT Exception handler

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<GenericResponse<String>> handleExpiredJwtException(ExpiredJwtException ex) {
        log.warn("JWT token has expired: {}", ex.getMessage());
        String message = messageSource.getMessage("error.jwt.expired", null, "Token JWT telah kedaluwarsa.",
                LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.UNAUTHORIZED, message);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ SignatureException.class, MalformedJwtException.class }) // Tangani signature atau format salah
    public ResponseEntity<GenericResponse<String>> handleInvalidJwtSignatureOrFormatException(JwtException ex) {
        log.warn("Invalid JWT token: {}", ex.getMessage());
        String message = messageSource.getMessage("error.jwt.invalid", null, "Token JWT tidak valid.",
                LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.UNAUTHORIZED, message);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<GenericResponse<String>> handleJwtException(JwtException ex) {
        log.error("Error processing JWT token: {}", ex.getMessage());
        String message = messageSource.getMessage("error.jwt.processing", null, "Terjadi masalah saat memproses token.",
                LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.UNAUTHORIZED, message);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Spring security exception

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GenericResponse<String>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        String message = messageSource.getMessage("error.auth.failed", new Object[] { ex.getMessage() },
                "Autentikasi gagal.", LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.UNAUTHORIZED, message);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        String message = messageSource.getMessage("error.access.denied", null, "Akses ditolak.",
                LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.FORBIDDEN, message);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<String>> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage("error.server.internal", null,
                "Terjadi kesalahan internal pada server.", LocaleContextHolder.getLocale());
        GenericResponse<String> response = GenericResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, message);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
