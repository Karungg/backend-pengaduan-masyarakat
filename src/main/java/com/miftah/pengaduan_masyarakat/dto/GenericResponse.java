package com.miftah.pengaduan_masyarakat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {

    private int code;

    private T data;

    public static <T> GenericResponse<T> ok(T data) {
        return new GenericResponse<>(HttpStatus.OK.value(), data);
    }

    public static <T> GenericResponse<T> created(T data) {
        return new GenericResponse<>(HttpStatus.CREATED.value(), data);
    }

    public static <T> GenericResponse<T> badRequest(T errorData) {
        return new GenericResponse<>(HttpStatus.BAD_REQUEST.value(), errorData);
    }

    public static GenericResponse<String> notFound(String message) {
        return new GenericResponse<>(HttpStatus.NOT_FOUND.value(), message);
    }

    public static <T> GenericResponse<T> error(HttpStatus status, T errorData) {
        return new GenericResponse<>(status.value(), errorData);
    }
}

