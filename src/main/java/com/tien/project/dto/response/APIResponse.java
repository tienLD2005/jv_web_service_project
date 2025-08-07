package com.tien.project.dto.response;

import com.tien.project.utils.TimeUtils;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@AllArgsConstructor
public class APIResponse<T> {
    private Boolean success;
    private String message;
    private DataWrapper<T> data;
    private List<ErrorDetail> errors;
    private String timestamp;


    // Constructor tự động gán timestamp
    public APIResponse(Boolean success, String message, DataWrapper<T> data, List<ErrorDetail> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.timestamp = TimeUtils.getCurrentTimestamp();
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataWrapper<U> {
        private List<U> items;
        private Pagination pagination;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pagination {
        private Integer currentPage;
        private Integer pageSize;
        private Integer totalPages;
        private Long totalItems;
    }
}

