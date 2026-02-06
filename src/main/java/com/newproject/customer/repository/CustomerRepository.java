package com.newproject.customer.repository;

import com.newproject.customer.domain.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByKeycloakUserId(String keycloakUserId);
}
