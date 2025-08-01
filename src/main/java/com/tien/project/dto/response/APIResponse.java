package com.tien.project.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private Object errors;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public static <T> APIResponse<T> success(T data, String message) {
        return new APIResponse<>(true, message, data, null, LocalDateTime.now());
    }

    public static <T> APIResponse<T> error(String message, Object errors) {
        return new APIResponse<>(false, message, null, errors, LocalDateTime.now());
    }
}