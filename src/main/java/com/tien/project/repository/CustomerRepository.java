package com.tien.project.repository;

import com.tien.project.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByUserId(Integer userId);
    Page<Customer> findByGroupId(Integer groupId, Pageable pageable);
    Page<Customer> findByStatus(Customer.CustomerStatus status, Pageable pageable);
    Page<Customer> findByGroupIdAndStatus(Integer groupId, Customer.CustomerStatus status, Pageable pageable);
}