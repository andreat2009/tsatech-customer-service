package com.newproject.customer.security;

import com.newproject.customer.domain.Customer;
import com.newproject.customer.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestActor {
    public static final String CUSTOMER_ID_HEADER = "X-Authenticated-Customer-Id";
    public static final String SUBJECT_HEADER = "X-Authenticated-Subject";

    private final CustomerRepository customerRepository;

    public RequestActor(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public boolean isAuthenticated() {
        Authentication authentication = authentication();
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public boolean isAdmin() {
        Authentication authentication = authentication();
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equalsIgnoreCase(authority.getAuthority()));
    }

    public Optional<String> subject() {
        HttpServletRequest request = currentRequest();
        if (request != null && StringUtils.hasText(request.getHeader(SUBJECT_HEADER))) {
            return Optional.of(request.getHeader(SUBJECT_HEADER).trim());
        }
        Authentication authentication = authentication();
        if (authentication == null) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt && StringUtils.hasText(jwt.getSubject())) {
            return Optional.of(jwt.getSubject().trim());
        }
        return Optional.empty();
    }

    public Optional<Long> currentCustomerId() {
        HttpServletRequest request = currentRequest();
        if (request != null && StringUtils.hasText(request.getHeader(CUSTOMER_ID_HEADER))) {
            try {
                return Optional.of(Long.parseLong(request.getHeader(CUSTOMER_ID_HEADER).trim()));
            } catch (NumberFormatException ex) {
                throw new AccessDeniedException("Invalid authenticated customer context");
            }
        }
        return currentCustomer().map(Customer::getId);
    }

    public Optional<Customer> currentCustomer() {
        return subject().flatMap(customerRepository::findByKeycloakUserId);
    }

    public Long resolveScopedCustomerId(Long requestedCustomerId) {
        if (!isAuthenticated() || isAdmin()) {
            return requestedCustomerId;
        }
        Long currentCustomerId = requireCurrentCustomerId();
        if (requestedCustomerId != null && !Objects.equals(requestedCustomerId, currentCustomerId)) {
            throw new AccessDeniedException("You cannot access another customer's resources");
        }
        return currentCustomerId;
    }

    public void assertCustomerAccessIfAuthenticated(Long requestedCustomerId) {
        if (!isAuthenticated() || isAdmin()) {
            return;
        }
        Long currentCustomerId = requireCurrentCustomerId();
        if (!Objects.equals(requestedCustomerId, currentCustomerId)) {
            throw new AccessDeniedException("You cannot access another customer's resources");
        }
    }

    private Long requireCurrentCustomerId() {
        return currentCustomerId()
            .orElseThrow(() -> new AccessDeniedException("Missing authenticated customer context"));
    }

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }
}
