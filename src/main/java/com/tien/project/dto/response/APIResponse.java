package com.tien.project.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse<T> {
    private Boolean success;
    private String message;
    private DataWrapper<T> data;
    private List<ErrorDetail> errors; // Tham chiếu đến ErrorDetail riêng biệt
    private String timestamp = LocalDateTime.now().toString();

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