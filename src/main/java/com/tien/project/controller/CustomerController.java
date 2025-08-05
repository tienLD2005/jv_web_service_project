package com.tien.project.controller;

import com.tien.project.dto.request.CustomerGroupRequest;
import com.tien.project.dto.request.CustomerRequest;
import com.tien.project.dto.request.UserRoleRequest;
import com.tien.project.dto.response.APIResponse;
import com.tien.project.entity.Customer;
import com.tien.project.entity.CustomerGroup;
import com.tien.project.entity.Role;
import com.tien.project.entity.UserRole;
import com.tien.project.service.CustomerService;
import com.tien.project.service.UserRoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserRoleService userRoleService;

    // Get role details
    @GetMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Role>> getRoleDetails(@PathVariable Integer roleId) {
        Role role = userRoleService.getRoleDetails(roleId);
        APIResponse.DataWrapper<Role> data = new APIResponse.DataWrapper<>(List.of(role), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Role details retrieved successfully", data, null, null), HttpStatus.OK);
    }

    // Assign role to user
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<UserRole>> assignRole(@Valid @RequestBody UserRoleRequest request) {
        UserRole userRole = userRoleService.assignRole(request.getUserId(), request.getRoleId());
        APIResponse.DataWrapper<UserRole> data = new APIResponse.DataWrapper<>(List.of(userRole), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Role assigned successfully", data, null, null), HttpStatus.CREATED);
    }

    // Update role assignment
    @PutMapping("/roles/{userRoleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<UserRole>> updateRoleAssignment(@PathVariable Integer userRoleId, @Valid @RequestBody UserRoleRequest request) {
        UserRole userRole = userRoleService.updateRoleAssignment(userRoleId, request.getRoleId());
        APIResponse.DataWrapper<UserRole> data = new APIResponse.DataWrapper<>(List.of(userRole), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Role assignment updated successfully", data, null, null), HttpStatus.OK);
    }

    // Revoke role from user
    @DeleteMapping("/roles/{userRoleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> revokeRole(@PathVariable Integer userRoleId) {
        userRoleService.revokeRole(userRoleId);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Role revoked"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Role revoked successfully", data, null, null), HttpStatus.OK);
    }

    // Get customer groups
    @GetMapping("/groups")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<APIResponse<CustomerGroup>> getCustomerGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CustomerGroup> groupPage = customerService.getCustomerGroups(PageRequest.of(page, size));
        APIResponse.Pagination pagination = new APIResponse.Pagination(page, size, groupPage.getTotalPages(), groupPage.getTotalElements());
        APIResponse.DataWrapper<CustomerGroup> data = new APIResponse.DataWrapper<>(groupPage.getContent(), pagination);
        return new ResponseEntity<>(new APIResponse<>(true, "Customer groups retrieved successfully", data, null, null), HttpStatus.OK);
    }

    // Create customer group
    @PostMapping("/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<CustomerGroup>> createCustomerGroup(@Valid @RequestBody CustomerGroupRequest request) {
        CustomerGroup group = customerService.createCustomerGroup(request);
        APIResponse.DataWrapper<CustomerGroup> data = new APIResponse.DataWrapper<>(List.of(group), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Customer group created successfully", data, null, null), HttpStatus.CREATED);
    }

    // Update customer group
    @PutMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<CustomerGroup>> updateCustomerGroup(@PathVariable Integer groupId, @Valid @RequestBody CustomerGroupRequest request) {
        CustomerGroup group = customerService.updateCustomerGroup(groupId, request);
        APIResponse.DataWrapper<CustomerGroup> data = new APIResponse.DataWrapper<>(List.of(group), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Customer group updated successfully", data, null, null), HttpStatus.OK);
    }

    // Delete customer group
    @DeleteMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deleteCustomerGroup(@PathVariable Integer groupId) {
        customerService.deleteCustomerGroup(groupId);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Customer group deleted"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Customer group deleted successfully", data, null, null), HttpStatus.OK);
    }

    // Get customers with filter and pagination
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<APIResponse<Customer>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer groupId,
            Authentication authentication) {
        String username = authentication.getName();
        Page<Customer> customerPage = customerService.getCustomers(PageRequest.of(page, size), status, groupId, username);
        APIResponse.Pagination pagination = new APIResponse.Pagination(page, size, customerPage.getTotalPages(), customerPage.getTotalElements());
        APIResponse.DataWrapper<Customer> data = new APIResponse.DataWrapper<>(customerPage.getContent(), pagination);
        return new ResponseEntity<>(new APIResponse<>(true, "Customers retrieved successfully", data, null, null), HttpStatus.OK);
    }

    // Get customer details
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<APIResponse<Customer>> getCustomerDetails(@PathVariable Integer customerId, Authentication authentication) {
        Customer customer = customerService.getCustomerDetails(customerId, authentication.getName());
        APIResponse.DataWrapper<Customer> data = new APIResponse.DataWrapper<>(List.of(customer), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Customer details retrieved successfully", data, null, null), HttpStatus.OK);
    }

    // Create customer
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Customer>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        APIResponse.DataWrapper<Customer> data = new APIResponse.DataWrapper<>(List.of(customer), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Customer created successfully", data, null, null), HttpStatus.CREATED);
    }

    // Update customer
    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<APIResponse<Customer>> updateCustomer(@PathVariable Integer customerId, @Valid @RequestBody CustomerRequest request, Authentication authentication) {
        Customer customer = customerService.updateCustomer(customerId, request, authentication.getName());
        APIResponse.DataWrapper<Customer> data = new APIResponse.DataWrapper<>(List.of(customer), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Customer updated successfully", data, null, null), HttpStatus.OK);
    }

    // Update customer status
    @PutMapping("/{customerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> updateCustomerStatus(@PathVariable Integer customerId, @RequestBody Customer.CustomerStatus status) {
        customerService.updateCustomerStatus(customerId, status);
        APIResponse.DataWrapper<String> data = new APIResponse.DataWrapper<>(List.of("Customer status updated"), null);
        return new ResponseEntity<>(new APIResponse<>(true, "Customer status updated successfully", data, null, null), HttpStatus.OK);
    }
}