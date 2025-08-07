package com.tien.project.controller;

import com.tien.project.dto.request.CustomerGroupRequest;
import com.tien.project.dto.request.CustomerRequest;
import com.tien.project.dto.request.PurchaseRequest;
import com.tien.project.dto.request.UserRoleRequest;
import com.tien.project.dto.response.APIResponse;
import com.tien.project.dto.response.ErrorDetail;
import com.tien.project.entity.*;
import com.tien.project.service.CustomerService;
import com.tien.project.service.PurchaseService;
import com.tien.project.service.UserRoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.tien.project.utils.ValidationUtils.extractErrors;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PurchaseService purchaseService;

    /// ======================= PHÂN QUYỀN CHO USER (START)=====================

    @GetMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Role>> getRoleDetails(@PathVariable Integer roleId) {
        try {
            Role role = userRoleService.getRoleDetails(roleId);
            APIResponse.DataWrapper<Role> data = new APIResponse.DataWrapper<>(List.of(role), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Lấy thông tin chi tiết vai trò thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<UserRole>> assignRole(@Valid @RequestBody UserRoleRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, extractErrors(result)));
        }
        try {
            UserRole userRole = userRoleService.assignRole(request.getUserId(), request.getRoleId());
            APIResponse.DataWrapper<UserRole> data = new APIResponse.DataWrapper<>(List.of(userRole), null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new APIResponse<>(true, "Gán vai trò thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PutMapping("/roles/{userRoleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<UserRole>> updateRoleAssignment(@PathVariable Integer userRoleId, @Valid @RequestBody UserRoleRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, extractErrors(result)));
        }
        try {
            UserRole userRole = userRoleService.updateRoleAssignment(userRoleId, request.getRoleId());
            APIResponse.DataWrapper<UserRole> data = new APIResponse.DataWrapper<>(List.of(userRole), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Cập nhật vai trò thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @DeleteMapping("/roles/{userRoleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> revokeRole(@PathVariable Integer userRoleId) {
        try {
            userRoleService.revokeRole(userRoleId);
            APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Đã thu hồi vai trò"), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Thu hồi vai trò thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }
/// ======================= PHÂN QUYỀN CHO USER (END)=====================




    /// ======================= QUẢN LÝ KHÁCH HÀNG (START)=====================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<APIResponse<Customer>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer groupId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Page<Customer> customerPage = customerService.getCustomers(PageRequest.of(page, size), status, groupId, username);
            APIResponse.Pagination pagination = new APIResponse.Pagination(page, size, customerPage.getTotalPages(), customerPage.getTotalElements());
            APIResponse.DataWrapper<Customer> data = new APIResponse.DataWrapper<>(customerPage.getContent(), pagination);
            return ResponseEntity.ok(new APIResponse<>(true, "Lấy danh sách khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<APIResponse<Customer>> getCustomerDetails(@PathVariable Integer customerId, Authentication authentication) {
        try {
            Customer customer = customerService.getCustomerDetails(customerId, authentication.getName());
            APIResponse.DataWrapper<Customer> data = new APIResponse.DataWrapper<>(List.of(customer), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Lấy thông tin chi tiết khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Customer>> createCustomer(@Valid @RequestBody CustomerRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, extractErrors(result)));
        }
        try {
            Customer customer = customerService.createCustomer(request);
            APIResponse.DataWrapper<Customer> data = new APIResponse.DataWrapper<>(List.of(customer), null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new APIResponse<>(true, "Tạo khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<APIResponse<Customer>> updateCustomer(@PathVariable Integer customerId, @Valid @RequestBody CustomerRequest request, BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, extractErrors(result)));
        }
        try {
            Customer customer = customerService.updateCustomer(customerId, request, authentication.getName());
            APIResponse.DataWrapper<Customer> data = new APIResponse.DataWrapper<>(List.of(customer), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Cập nhật khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PutMapping("/{customerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> updateCustomerStatus(@PathVariable Integer customerId, @RequestBody Customer.CustomerStatus status) {
        try {
            customerService.updateCustomerStatus(customerId, status);
            APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Đã cập nhật trạng thái khách hàng"), null);
            return ResponseEntity.ok(new APIResponse<>(true, "Cập nhật trạng thái khách hàng thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> softDeleteCustomer(@PathVariable Integer customerId) {
        try {
            boolean deleted = customerService.softDeleteCustomer(customerId);
            APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of(deleted ? "Khách hàng đã được xóa mềm" : "Không tìm thấy khách hàng"), null);
            return ResponseEntity.status(deleted ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>(deleted, deleted ? "Khách hàng đã được xóa mềm" : "Không tìm thấy khách hàng", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    /// ======================= QUẢN LÝ LỊCH SỬ GIAO DỊCH CHO KHÁCH HÀNG (START)=====================


    @GetMapping("/{customerId}/purchases")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<APIResponse<Purchase>> getPurchases(@PathVariable Integer customerId) {
        try {
            List<Purchase> purchases = purchaseService.getPurchasesByCustomerId(customerId);
            APIResponse.DataWrapper<Purchase> data = new APIResponse.DataWrapper<>(purchases, null);
            return ResponseEntity.ok(new APIResponse<>(true, "Lịch sử mua hàng đã lấy thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PostMapping("/{customerId}/purchases")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<APIResponse<Purchase>> addPurchase(@PathVariable Integer customerId, @Valid @RequestBody PurchaseRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, extractErrors(result)));
        }
        try {
            Purchase purchase = purchaseService.addPurchase(customerId, request);
            APIResponse.DataWrapper<Purchase> data = new APIResponse.DataWrapper<>(List.of(purchase), null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new APIResponse<>(true, "Thêm giao dịch thành công", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @PutMapping("/{customerId}/purchases/{purchaseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<APIResponse<Purchase>> updatePurchase(@PathVariable Integer customerId, @PathVariable Integer purchaseId, @Valid @RequestBody PurchaseRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, "Dữ liệu không hợp lệ", null, extractErrors(result)));
        }
        try {
            Purchase purchase = purchaseService.updatePurchase(customerId, purchaseId, request);
            APIResponse.DataWrapper<Purchase> data = new APIResponse.DataWrapper<>(purchase != null ? List.of(purchase) : List.of(), null);
            return ResponseEntity.status(purchase != null ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>(purchase != null, purchase != null ? "Cập nhật giao dịch thành công" : "Không tìm thấy giao dịch", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    @DeleteMapping("/{customerId}/purchases/{purchaseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deletePurchase(@PathVariable Integer customerId, @PathVariable Integer purchaseId) {
        try {
            boolean deleted = purchaseService.deletePurchase(customerId, purchaseId);
            APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of(deleted ? "Đã xóa giao dịch mua hàng" : "Không tìm thấy giao dịch mua hàng"), null);
            return ResponseEntity.status(deleted ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                    .body(new APIResponse<>(deleted, deleted ? "Xóa giao dịch thành công" : "Không tìm thấy giao dịch", data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(false, e.getMessage(), null, null));
        }
    }

    /// ======================= QUẢN LÝ LỊCH SỬ GIAO DỊCH CHO KHÁCH HÀNG (END)=====================


}