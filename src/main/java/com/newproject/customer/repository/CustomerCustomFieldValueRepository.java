package com.newproject.customer.repository;

import com.newproject.customer.domain.CustomerCustomFieldValue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerCustomFieldValueRepository extends JpaRepository<CustomerCustomFieldValue, Long> {
    List<CustomerCustomFieldValue> findByCustomerId(Long customerId);

    Optional<CustomerCustomFieldValue> findByCustomerIdAndCustomFieldId(Long customerId, Long customFieldId);
}
