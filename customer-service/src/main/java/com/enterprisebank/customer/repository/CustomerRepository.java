package com.enterprisebank.customer.repository;

import com.enterprisebank.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerNumber(String customerNumber);

    Optional<Customer> findByAuthUserId(Long authUserId);

    Optional<Customer> findByEmail(String email);

    boolean existsByAuthUserId(Long authUserId);

    boolean existsByEmail(String email);

    boolean existsByCustomerNumber(String customerNumber);
}