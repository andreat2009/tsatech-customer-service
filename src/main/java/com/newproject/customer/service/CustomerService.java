package com.newproject.customer.service;

import com.newproject.customer.domain.Customer;
import com.newproject.customer.dto.CustomerRequest;
import com.newproject.customer.dto.CustomerResponse;
import com.newproject.customer.events.EventPublisher;
import com.newproject.customer.exception.BadRequestException;
import com.newproject.customer.exception.NotFoundException;
import com.newproject.customer.repository.CustomerRepository;
import com.newproject.customer.security.RequestActor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    public static final String DEFAULT_CUSTOMER_GROUP = "RETAIL";

    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisher;
    private final RequestActor requestActor;

    public CustomerService(CustomerRepository customerRepository, EventPublisher eventPublisher, RequestActor requestActor) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
        this.requestActor = requestActor;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String normalizedEmail = request.getEmail() != null ? request.getEmail().toLowerCase(Locale.ROOT) : null;
        String keycloakUserId = normalizeKeycloakUserId(request.getKeycloakUserId());

        if (requestActor.isAuthenticated() && !requestActor.isAdmin()) {
            String actorSubject = requestActor.subject().orElse(null);
            if (actorSubject != null) {
                if (keycloakUserId != null && !actorSubject.equals(keycloakUserId)) {
                    throw new BadRequestException("Authenticated users can only manage their own customer profile");
                }
                keycloakUserId = actorSubject;
            }
        }

        Customer existingByEmail = normalizedEmail == null ? null : customerRepository.findByEmail(normalizedEmail).orElse(null);
        if (existingByEmail != null) {
            if (keycloakUserId == null) {
                requestActor.assertCustomerAccessIfAuthenticated(existingByEmail.getId());
                return toResponse(existingByEmail);
            }
            if (existingByEmail.getKeycloakUserId() != null && !keycloakUserId.equals(existingByEmail.getKeycloakUserId())) {
                throw new BadRequestException("Email already exists");
            }
            requestActor.assertCustomerAccessIfAuthenticated(existingByEmail.getId());
            existingByEmail.setKeycloakUserId(keycloakUserId);
            existingByEmail.setUpdatedAt(OffsetDateTime.now());
            Customer saved = customerRepository.save(existingByEmail);
            eventPublisher.publish("CUSTOMER_UPDATED", "customer", saved.getId().toString(), toResponse(saved));
            return toResponse(saved);
        }

        if (keycloakUserId != null) {
            customerRepository.findByKeycloakUserId(keycloakUserId)
                .ifPresent(existing -> {
                    throw new BadRequestException("Keycloak user already linked");
                });
        }

        Customer customer = new Customer();
        request.setKeycloakUserId(keycloakUserId);
        request.setEmail(normalizedEmail);
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
        requestActor.assertCustomerAccessIfAuthenticated(id);
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

        customerRepository.findByEmail(request.getEmail())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new BadRequestException("Email already exists");
            });

        String keycloakUserId = normalizeKeycloakUserId(request.getKeycloakUserId());
        if (requestActor.isAuthenticated() && !requestActor.isAdmin()) {
            String actorSubject = requestActor.subject().orElse(null);
            if (actorSubject != null) {
                if (keycloakUserId != null && !actorSubject.equals(keycloakUserId)) {
                    throw new BadRequestException("Authenticated users can only manage their own customer profile");
                }
                keycloakUserId = actorSubject;
            }
        }
        request.setKeycloakUserId(keycloakUserId);

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
        requestActor.assertCustomerAccessIfAuthenticated(id);
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> list(String email, String keycloakUserId, String customerGroupCode, Boolean active) {
        if (requestActor.isAuthenticated() && !requestActor.isAdmin()) {
            return requestActor.currentCustomer()
                .map(this::toResponse)
                .map(List::of)
                .orElse(List.of());
        }

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

        String normalizedGroup = normalizeCustomerGroupCodeFilter(customerGroupCode);
        return customerRepository.findAll().stream()
            .filter(customer -> active == null || active.equals(customer.getActive()))
            .filter(customer -> normalizedGroup == null || normalizedGroup.equals(normalizeCustomerGroupCode(customer.getCustomerGroupCode())))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        requestActor.assertCustomerAccessIfAuthenticated(id);
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        customerRepository.delete(customer);
        eventPublisher.publish("CUSTOMER_DELETED", "customer", id.toString(), null);
    }

    private void applyRequest(Customer customer, CustomerRequest request) {
        customer.setKeycloakUserId(normalizeKeycloakUserId(request.getKeycloakUserId()));
        customer.setEmail(request.getEmail() != null ? request.getEmail().toLowerCase(Locale.ROOT) : null);
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setCustomerGroupCode(normalizeCustomerGroupCode(request.getCustomerGroupCode()));
        customer.setActive(request.getActive());
    }

    private String normalizeKeycloakUserId(String keycloakUserId) {
        if (keycloakUserId == null) {
            return null;
        }
        String trimmed = keycloakUserId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeCustomerGroupCode(String customerGroupCode) {
        if (customerGroupCode == null || customerGroupCode.isBlank()) {
            return DEFAULT_CUSTOMER_GROUP;
        }
        return customerGroupCode.trim().replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private String normalizeCustomerGroupCodeFilter(String customerGroupCode) {
        if (customerGroupCode == null || customerGroupCode.isBlank()) {
            return null;
        }
        return normalizeCustomerGroupCode(customerGroupCode);
    }

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setKeycloakUserId(customer.getKeycloakUserId());
        response.setEmail(customer.getEmail());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setPhone(customer.getPhone());
        response.setCustomerGroupCode(normalizeCustomerGroupCode(customer.getCustomerGroupCode()));
        response.setActive(customer.getActive());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }
}
