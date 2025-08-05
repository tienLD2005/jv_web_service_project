package com.tien.project.service;

import com.tien.project.dto.request.CustomerGroupRequest;
import com.tien.project.dto.request.CustomerRequest;
import com.tien.project.entity.Customer;
import com.tien.project.entity.CustomerGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<CustomerGroup> getCustomerGroups(Pageable pageable);
    CustomerGroup createCustomerGroup(CustomerGroupRequest request);
    CustomerGroup updateCustomerGroup(Integer groupId, CustomerGroupRequest request);
    void deleteCustomerGroup(Integer groupId);
    Page<Customer> getCustomers(Pageable pageable, String status, Integer groupId, String username);
    Customer getCustomerDetails(Integer customerId, String username);
    Customer createCustomer(CustomerRequest request);
    Customer updateCustomer(Integer customerId, CustomerRequest request, String username);
    void updateCustomerStatus(Integer customerId, Customer.CustomerStatus status);

    boolean softDeleteCustomer(Integer customerId);
}