package com.newproject.customer.service;

import com.newproject.customer.domain.Address;
import com.newproject.customer.domain.Customer;
import com.newproject.customer.dto.AddressRequest;
import com.newproject.customer.dto.AddressResponse;
import com.newproject.customer.events.EventPublisher;
import com.newproject.customer.exception.BadRequestException;
import com.newproject.customer.exception.NotFoundException;
import com.newproject.customer.repository.AddressRepository;
import com.newproject.customer.repository.CustomerRepository;
import com.newproject.customer.security.RequestActor;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisher;
    private final RequestActor requestActor;

    public AddressService(
        AddressRepository addressRepository,
        CustomerRepository customerRepository,
        EventPublisher eventPublisher,
        RequestActor requestActor
    ) {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
        this.requestActor = requestActor;
    }

    @Transactional
    public AddressResponse create(Long customerId, AddressRequest request) {
        requestActor.assertCustomerAccessIfAuthenticated(customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

        Address address = new Address();
        applyRequest(address, request);
        address.setCustomer(customer);

        Address saved = addressRepository.save(address);
        eventPublisher.publish("ADDRESS_CREATED", "address", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public AddressResponse update(Long addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new NotFoundException("Address not found"));
        requestActor.assertCustomerAccessIfAuthenticated(address.getCustomer().getId());
        applyRequest(address, request);
        Address saved = addressRepository.save(address);
        eventPublisher.publish("ADDRESS_UPDATED", "address", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AddressResponse get(Long addressId) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new NotFoundException("Address not found"));
        requestActor.assertCustomerAccessIfAuthenticated(address.getCustomer().getId());
        return toResponse(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listForCustomer(Long customerId) {
        requestActor.assertCustomerAccessIfAuthenticated(customerId);
        customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        return addressRepository.findByCustomerId(customerId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long addressId) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new NotFoundException("Address not found"));
        requestActor.assertCustomerAccessIfAuthenticated(address.getCustomer().getId());
        addressRepository.delete(address);
        eventPublisher.publish("ADDRESS_DELETED", "address", addressId.toString(), null);
    }

    private void applyRequest(Address address, AddressRequest request) {
        if (request.getIsDefault() == null) {
            throw new BadRequestException("isDefault is required");
        }
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setRegion(request.getRegion());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(request.getIsDefault());
    }

    private AddressResponse toResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setCustomerId(address.getCustomer().getId());
        response.setLine1(address.getLine1());
        response.setLine2(address.getLine2());
        response.setCity(address.getCity());
        response.setRegion(address.getRegion());
        response.setCountry(address.getCountry());
        response.setPostalCode(address.getPostalCode());
        response.setIsDefault(address.getIsDefault());
        return response;
    }
}
