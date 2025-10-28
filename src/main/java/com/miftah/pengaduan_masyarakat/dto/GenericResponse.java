package com.miftah.pengaduan_masyarakat.dto;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {

    private int code;

    private String status;

    private T data;

    private String message;

    private Map<String, List<String>> errors;

    public static <T> GenericResponse<T> ok(T data) {
        return GenericResponse.<T>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.getReasonPhrase())
                .data(data)
                .build();
    }

    public static <T> GenericResponse<T> created(T data) {
        return GenericResponse.<T>builder()
                .code(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED.getReasonPhrase())
                .data(data)
                .build();
    }

    public static <T> GenericResponse<T> noContent() {
        return GenericResponse.<T>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .status(HttpStatus.NO_CONTENT.getReasonPhrase())
                .build();
    }

    public static <T> GenericResponse<T> badRequest(Map<String, List<String>> errors, String message) {
        return GenericResponse.<T>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errors(errors)
                .message(message)
                .build();
    }

    public static <T> GenericResponse<T> notFound(String message) {
        return GenericResponse.<T>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .build();
    }

    public static <T> GenericResponse<T> unauthorized(String message) {
        return GenericResponse.<T>builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .status(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(message)
                .build();
    }

    public static <T> GenericResponse<T> forbidden(String message) {
        return GenericResponse.<T>builder()
                .code(HttpStatus.FORBIDDEN.value())
                .status(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(message)
                .build();
    }

    public static <T> GenericResponse<T> internalServerError(String message) {
        return GenericResponse.<T>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(message)
                .build();
    }
}
