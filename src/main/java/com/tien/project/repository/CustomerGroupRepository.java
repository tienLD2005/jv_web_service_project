package com.tien.project.repository;

import com.tien.project.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Integer> {
    Optional<CustomerGroup> findByGroupName(String groupName);

}