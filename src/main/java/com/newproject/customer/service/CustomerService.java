package com.newproject.customer.service;

import com.newproject.customer.domain.Customer;
import com.newproject.customer.dto.CustomerRequest;
import com.newproject.customer.dto.CustomerResponse;
import com.newproject.customer.events.EventPublisher;
import com.newproject.customer.exception.BadRequestException;
import com.newproject.customer.exception.NotFoundException;
import com.newproject.customer.repository.CustomerRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisher;

    public CustomerService(CustomerRepository customerRepository, EventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        customerRepository.findByEmail(request.getEmail())
            .ifPresent(existing -> {
                throw new BadRequestException("Email already exists");
            });

        if (request.getKeycloakUserId() != null) {
            customerRepository.findByKeycloakUserId(request.getKeycloakUserId())
                .ifPresent(existing -> {
                    throw new BadRequestException("Keycloak user already linked");
                });
        }

        Customer customer = new Customer();
        applyRequest(customer, request);
        OffsetDateTime now = OffsetDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        Customer saved = customerRepository.save(customer);
        eventPublisher.publish("CUSTOMER_CREATED", "customer", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

        customerRepository.findByEmail(request.getEmail())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new BadRequestException("Email already exists");
            });

        if (request.getKeycloakUserId() != null) {
            customerRepository.findByKeycloakUserId(request.getKeycloakUserId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Keycloak user already linked");
                });
        }

        applyRequest(customer, request);
        customer.setUpdatedAt(OffsetDateTime.now());

        Customer saved = customerRepository.save(customer);
        eventPublisher.publish("CUSTOMER_UPDATED", "customer", saved.getId().toString(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> list(String email, String keycloakUserId, Boolean active) {
        if (email != null && !email.isBlank()) {
            return customerRepository.findByEmail(email.toLowerCase(Locale.ROOT))
                .map(this::toResponse)
                .map(List::of)
                .orElse(List.of());
        }

        if (keycloakUserId != null && !keycloakUserId.isBlank()) {
            return customerRepository.findByKeycloakUserId(keycloakUserId)
                .map(this::toResponse)
                .map(List::of)
                .orElse(List.of());
        }

        return customerRepository.findAll().stream()
            .filter(customer -> active == null || active.equals(customer.getActive()))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        customerRepository.delete(customer);
        eventPublisher.publish("CUSTOMER_DELETED", "customer", id.toString(), null);
    }

    private void applyRequest(Customer customer, CustomerRequest request) {
        customer.setKeycloakUserId(request.getKeycloakUserId());
        customer.setEmail(request.getEmail() != null ? request.getEmail().toLowerCase(Locale.ROOT) : null);
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setActive(request.getActive());
    }

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setKeycloakUserId(customer.getKeycloakUserId());
        response.setEmail(customer.getEmail());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setPhone(customer.getPhone());
        response.setActive(customer.getActive());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }
}
