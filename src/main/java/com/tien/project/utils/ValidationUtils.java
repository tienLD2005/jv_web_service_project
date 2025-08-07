package com.tien.project.utils;

import com.tien.project.dto.response.ErrorDetail;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationUtils {
    public static List<ErrorDetail> extractErrors(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
    }
}