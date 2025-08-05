package com.tien.project.controller;

import com.tien.project.dto.response.APIResponse;
import com.tien.project.dto.response.ErrorDetail;
import com.tien.project.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics/sales")
public class SalesAnalyticsController {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<APIResponse<Map<String, Object>>> getRevenueReportBetween(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Authentication authentication
    ) {
        try {
            // Log user roles and parameters for debugging
            System.out.println("User roles for /revenue: " + authentication.getAuthorities());
            System.out.println("Request parameters: start=" + start + ", end=" + end);

            // Kiểm tra xem start có sau end không
            if (start.isAfter(end)) {
                APIResponse<Map<String, Object>> errorResponse = new APIResponse<>(
                        false,
                        "Ngày bắt đầu không được sau ngày kết thúc",
                        null,
                        List.of(new ErrorDetail("INVALID_DATE_RANGE", "Start date must not be after end date")),
                        null
                );
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // Chuyển end sang LocalDateTime, cộng thêm 1 ngày
            LocalDateTime endPlusOne = end.plusDays(1).atStartOfDay();

            List<Map<String, Object>> report = purchaseRepository.getRevenueReportBetween(start, endPlusOne);
            APIResponse.DataWrapper<Map<String, Object>> data = new APIResponse.DataWrapper<>(report, null);
            APIResponse<Map<String, Object>> response = new APIResponse<>(true, "Lấy báo cáo doanh thu theo ngày thành công", data, null, null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            APIResponse<Map<String, Object>> errorResponse = new APIResponse<>(
                    false,
                    "Định dạng ngày không hợp lệ cho tham số start hoặc end",
                    null,
                    List.of(new ErrorDetail("INVALID_DATE", e.getMessage())),
                    null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            APIResponse<Map<String, Object>> errorResponse = new APIResponse<>(
                    false,
                    "Lỗi khi lấy báo cáo doanh thu: " + e.getMessage(),
                    null,
                    List.of(new ErrorDetail("ERROR", e.getMessage())),
                    null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/top-customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<APIResponse<Map<String, Object>>> getTopCustomers(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication
    ) {
        // Log user roles and parameter for debugging
        System.out.println("User roles for /top-customers: " + authentication.getAuthorities());
        System.out.println("Request parameter: limit=" + limit);

        List<Map<String, Object>> topCustomers = purchaseRepository.getTopCustomers(limit);
        APIResponse.DataWrapper<Map<String, Object>> data = new APIResponse.DataWrapper<>(topCustomers, null);
        APIResponse<Map<String, Object>> response = new APIResponse<>(true, "Lấy danh sách khách hàng chi tiêu nhiều nhất thành công", data, null, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<APIResponse<Map<String, Object>>> handleMissingParams(MissingServletRequestParameterException ex) {
        APIResponse<Map<String, Object>> errorResponse = new APIResponse<>(
                false,
                "Thiếu tham số bắt buộc: " + ex.getParameterName(),
                null,
                List.of(new ErrorDetail("MISSING_PARAMETER", ex.getMessage())),
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}