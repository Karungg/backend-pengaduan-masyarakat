package com.miftah.pengaduan_masyarakat.exception;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    private final Map<String, List<String>> errors;

    public ValidationException(Map<String, List<String>> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public ValidationException(String message, Map<String, List<String>> errors) {
        super(message);
        this.errors = errors;
    }
}
