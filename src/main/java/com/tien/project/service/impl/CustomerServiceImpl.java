package com.tien.project.service.impl;

import com.tien.project.dto.request.CustomerGroupRequest;
import com.tien.project.dto.request.CustomerRequest;
import com.tien.project.entity.Customer;
import com.tien.project.entity.CustomerGroup;
import com.tien.project.entity.User;
import com.tien.project.repository.CustomerGroupRepository;
import com.tien.project.repository.CustomerRepository;
import com.tien.project.repository.UserRepository;
import com.tien.project.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerGroupRepository customerGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<CustomerGroup> getCustomerGroups(Pageable pageable) {
        return customerGroupRepository.findAll(pageable);
    }

    @Override
    public CustomerGroup createCustomerGroup(CustomerGroupRequest request) {
        CustomerGroup group = new CustomerGroup();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        return customerGroupRepository.save(group);
    }

    @Override
    public CustomerGroup updateCustomerGroup(Integer groupId, CustomerGroupRequest request) {
        CustomerGroup group = customerGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Customer group not found"));
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setUpdatedAt(LocalDateTime.now());
        return customerGroupRepository.save(group);
    }

    @Override
    public void deleteCustomerGroup(Integer groupId) {
        CustomerGroup group = customerGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Customer group not found"));
        customerGroupRepository.delete(group);
    }

    @Override
    public Page<Customer> getCustomers(Pageable pageable, String status, Integer groupId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRoles().stream().noneMatch(role -> role.getRoleName().equals("ADMIN") || role.getRoleName().equals("STAFF"))) {
            Optional<Customer> customerOpt = customerRepository.findByUserId(user.getUserId());
            if (customerOpt.isEmpty()) {
                throw new EntityNotFoundException("Customer not found");
            }
            List<Customer> customerList = Collections.singletonList(customerOpt.get());
            return new PageImpl<>(customerList, pageable, customerList.size());
        }

        Customer.CustomerStatus customerStatus = status != null ? Customer.CustomerStatus.valueOf(status.toUpperCase()) : null;
        if (groupId != null && customerStatus != null) {
            return customerRepository.findByGroupIdAndStatus(groupId, customerStatus, pageable);
        } else if (groupId != null) {
            return customerRepository.findByGroupId(groupId, pageable);
        } else if (customerStatus != null) {
            return customerRepository.findByStatus(customerStatus, pageable);
        }
        return customerRepository.findAll(pageable);
    }

    @Override
    public Customer getCustomerDetails(Integer customerId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (user.getRoles().stream().noneMatch(role -> role.getRoleName().equals("ADMIN") || role.getRoleName().equals("STAFF"))
                && !Objects.equals(customer.getUserId(), user.getUserId())) {
            throw new AccessDeniedException("You can only view your own customer details");
        }
        return customer;
    }

    @Override
    public Customer createCustomer(CustomerRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (customerRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalStateException("Customer already exists for this user");
        }

        Customer customer = new Customer();
        customer.setUserId(request.getUserId());
        customer.setGroupId(request.getGroupId());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setCountry(request.getCountry());
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Integer customerId, CustomerRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (user.getRoles().stream().noneMatch(role -> role.getRoleName().equals("ADMIN"))
                && !Objects.equals(customer.getUserId(), user.getUserId())) {
            throw new AccessDeniedException("You can only update your own customer details");
        }

        customer.setGroupId(request.getGroupId());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setCountry(request.getCountry());
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    @Override
    public void updateCustomerStatus(Integer customerId, Customer.CustomerStatus status) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        customer.setStatus(status);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    @Override
    public boolean softDeleteCustomer(Integer customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            customer.setStatus(Customer.CustomerStatus.INACTIVE);
            customer.setUpdatedAt(LocalDateTime.now());
            customerRepository.save(customer);
            return true;
        }
        return false;
    }
}