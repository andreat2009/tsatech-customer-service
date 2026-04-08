package com.newproject.customer.repository;

import com.newproject.customer.domain.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long customerId);
    List<Address> findByCustomerIdAndAddressTypeIgnoreCase(Long customerId, String addressType);
    Optional<Address> findFirstByCustomerIdAndAddressTypeIgnoreCase(Long customerId, String addressType);
}
