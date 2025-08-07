package com.tien.project.controller;

import com.tien.project.dto.request.CustomerGroupRequest;
import com.tien.project.dto.request.UserRoleRequest;
import com.tien.project.dto.response.APIResponse;
import com.tien.project.dto.response.ErrorDetail;
import com.tien.project.entity.*;
import com.tien.project.service.CustomerService;
import com.tien.project.service.UserRoleService;
import com.tien.project.utils.ValidationUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-groups")
public class CustomerGroupController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<APIResponse<CustomerGroup>> getCustomerGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<CustomerGroup> groupPage = customerService.getCustomerGroups(PageRequest.of(page, size));
            APIResponse.Pagination pagination = new APIResponse.Pagination(page, size, groupPage.getTotalPages(), groupPage.getTotalElements());
            APIResponse.DataWrapper<CustomerGroup> data = new APIResponse.DataWrapper<>(groupPage.getContent(), pagination);
            return ResponseEntity.ok(new APIResponse<>(true, "Lấy nhóm khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<CustomerGroup>> createCustomerGroup(@Valid @RequestBody CustomerGroupRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, ValidationUtils.extractErrors(result)));
        }
        try {
            CustomerGroup group = customerService.createCustomerGroup(request);
            APIResponse.DataWrapper<CustomerGroup> data = new APIResponse.DataWrapper<>(List.of(group), null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new APIResponse<>(true, "Tạo nhóm khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PutMapping("/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<CustomerGroup>> updateCustomerGroup(@PathVariable Integer groupId, @Valid @RequestBody CustomerGroupRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, ValidationUtils.extractErrors(result)));
        }
        try {
            CustomerGroup group = customerService.updateCustomerGroup(groupId, request);
            APIResponse.DataWrapper<CustomerGroup> data = new APIResponse.DataWrapper<>(List.of(group), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Cập nhật nhóm khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deleteCustomerGroup(@PathVariable Integer groupId) {
        try {
            customerService.deleteCustomerGroup(groupId);
            APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Đã xóa nhóm khách hàng"), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Xóa nhóm khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }
}