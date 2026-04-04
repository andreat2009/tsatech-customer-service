package com.newproject.customer.service;

import com.newproject.customer.domain.CustomFieldDefinition;
import com.newproject.customer.domain.CustomFieldOption;
import com.newproject.customer.domain.Customer;
import com.newproject.customer.domain.CustomerCustomFieldValue;
import com.newproject.customer.dto.*;
import com.newproject.customer.exception.BadRequestException;
import com.newproject.customer.exception.NotFoundException;
import com.newproject.customer.repository.CustomFieldDefinitionRepository;
import com.newproject.customer.repository.CustomerCustomFieldValueRepository;
import com.newproject.customer.repository.CustomerRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomFieldService {
    private final CustomFieldDefinitionRepository definitionRepository;
    private final CustomerCustomFieldValueRepository valueRepository;
    private final CustomerRepository customerRepository;

    public CustomFieldService(
        CustomFieldDefinitionRepository definitionRepository,
        CustomerCustomFieldValueRepository valueRepository,
        CustomerRepository customerRepository
    ) {
        this.definitionRepository = definitionRepository;
        this.valueRepository = valueRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomFieldDefinitionResponse> listDefinitions(String scope, Boolean active) {
        List<CustomFieldDefinition> definitions;
        if (scope != null && !scope.isBlank() && active != null) {
            definitions = definitionRepository.findByFieldScopeAndActiveOrderBySortOrderAscCodeAsc(scope.toUpperCase(Locale.ROOT), active);
        } else if (scope != null && !scope.isBlank()) {
            definitions = definitionRepository.findByFieldScopeOrderBySortOrderAscCodeAsc(scope.toUpperCase(Locale.ROOT));
        } else if (active != null) {
            definitions = definitionRepository.findByActiveOrderBySortOrderAscCodeAsc(active);
        } else {
            definitions = definitionRepository.findAll().stream()
                .sorted(Comparator.comparing(CustomFieldDefinition::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(CustomFieldDefinition::getCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        }
        return definitions.stream().map(this::toDefinitionResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomFieldDefinitionResponse getDefinition(Long id) {
        return toDefinitionResponse(definitionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Custom field not found")));
    }

    @Transactional
    public CustomFieldDefinitionResponse createDefinition(CustomFieldDefinitionRequest request) {
        validateDefinitionRequest(request, null);
        CustomFieldDefinition definition = new CustomFieldDefinition();
        applyDefinition(definition, request);
        return toDefinitionResponse(definitionRepository.save(definition));
    }

    @Transactional
    public CustomFieldDefinitionResponse updateDefinition(Long id, CustomFieldDefinitionRequest request) {
        CustomFieldDefinition definition = definitionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Custom field not found"));
        validateDefinitionRequest(request, id);
        applyDefinition(definition, request);
        return toDefinitionResponse(definitionRepository.save(definition));
    }

    @Transactional
    public void deleteDefinition(Long id) {
        CustomFieldDefinition definition = definitionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Custom field not found"));
        definitionRepository.delete(definition);
    }

    @Transactional(readOnly = true)
    public List<CustomerCustomFieldValueResponse> listValues(Long customerId, String scope) {
        customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

        String normalizedScope = normalizeNullable(scope);
        return valueRepository.findByCustomerId(customerId).stream()
            .filter(value -> normalizedScope == null || normalizedScope.equals(value.getCustomField().getFieldScope()))
            .sorted(Comparator.comparing(v -> Optional.ofNullable(v.getCustomField().getSortOrder()).orElse(0)))
            .map(this::toValueResponse)
            .toList();
    }

    @Transactional
    public List<CustomerCustomFieldValueResponse> upsertValues(Long customerId, List<CustomerCustomFieldValueRequest> requests) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        if (requests == null) {
            return List.of();
        }

        List<CustomerCustomFieldValueResponse> responses = new ArrayList<>();
        for (CustomerCustomFieldValueRequest request : requests) {
            CustomFieldDefinition definition = resolveDefinition(request);
            if (!Boolean.TRUE.equals(definition.getPersistForCustomer())) {
                throw new BadRequestException("Custom field is not configured for customer persistence: " + definition.getCode());
            }

            CustomerCustomFieldValue value = valueRepository
                .findByCustomerIdAndCustomFieldId(customerId, definition.getId())
                .orElseGet(CustomerCustomFieldValue::new);

            boolean created = value.getId() == null;
            OffsetDateTime now = OffsetDateTime.now();
            value.setCustomer(customer);
            value.setCustomField(definition);
            value.setFieldValue(trimToNull(request.getValue()));
            if (created) {
                value.setCreatedAt(now);
            }
            value.setUpdatedAt(now);
            responses.add(toValueResponse(valueRepository.save(value)));
        }
        return responses;
    }

    private CustomFieldDefinition resolveDefinition(CustomerCustomFieldValueRequest request) {
        if (request == null) {
            throw new BadRequestException("Custom field value request is required");
        }
        if (request.getCustomFieldId() != null) {
            return definitionRepository.findById(request.getCustomFieldId())
                .orElseThrow(() -> new NotFoundException("Custom field not found"));
        }
        String code = normalizeNullable(request.getCustomFieldCode());
        if (code == null) {
            throw new BadRequestException("customFieldId or customFieldCode is required");
        }
        return definitionRepository.findByCode(code)
            .orElseThrow(() -> new NotFoundException("Custom field not found"));
    }

    private void validateDefinitionRequest(CustomFieldDefinitionRequest request, Long currentId) {
        if (request == null) {
            throw new BadRequestException("Custom field definition is required");
        }
        String code = normalizeNullable(request.getCode());
        if (code == null) {
            throw new BadRequestException("code is required");
        }
        definitionRepository.findByCode(code)
            .filter(existing -> !Objects.equals(existing.getId(), currentId))
            .ifPresent(existing -> {
                throw new BadRequestException("Custom field code already exists");
            });

        if (normalizeNullable(request.getLabel()) == null) {
            throw new BadRequestException("label is required");
        }
        if (normalizeNullable(request.getFieldType()) == null) {
            throw new BadRequestException("fieldType is required");
        }
        if (normalizeNullable(request.getFieldScope()) == null) {
            throw new BadRequestException("fieldScope is required");
        }
    }

    private void applyDefinition(CustomFieldDefinition definition, CustomFieldDefinitionRequest request) {
        definition.setCode(normalizeNullable(request.getCode()));
        definition.setLabel(trimToNull(request.getLabel()));
        definition.setPlaceholder(trimToNull(request.getPlaceholder()));
        definition.setHelpText(trimToNull(request.getHelpText()));
        definition.setFieldType(normalizeNullable(request.getFieldType()));
        definition.setFieldScope(normalizeNullable(request.getFieldScope()));
        definition.setRequired(Boolean.TRUE.equals(request.getRequired()));
        definition.setActive(request.getActive() == null || request.getActive());
        definition.setPersistForCustomer(request.getPersistForCustomer() == null || request.getPersistForCustomer());
        definition.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        definition.setOptions(toOptions(request.getOptions()));
    }

    private List<CustomFieldOption> toOptions(List<CustomFieldOptionRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        List<CustomFieldOption> options = new ArrayList<>();
        for (CustomFieldOptionRequest request : requests) {
            String optionValue = normalizeNullable(request.getOptionValue());
            String label = trimToNull(request.getLabel());
            if (optionValue == null || label == null) {
                continue;
            }
            CustomFieldOption option = new CustomFieldOption();
            option.setOptionValue(optionValue);
            option.setLabel(label);
            option.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
            options.add(option);
        }
        return options;
    }

    private CustomFieldDefinitionResponse toDefinitionResponse(CustomFieldDefinition definition) {
        CustomFieldDefinitionResponse response = new CustomFieldDefinitionResponse();
        response.setId(definition.getId());
        response.setCode(definition.getCode());
        response.setLabel(definition.getLabel());
        response.setPlaceholder(definition.getPlaceholder());
        response.setHelpText(definition.getHelpText());
        response.setFieldType(definition.getFieldType());
        response.setFieldScope(definition.getFieldScope());
        response.setRequired(definition.getRequired());
        response.setActive(definition.getActive());
        response.setPersistForCustomer(definition.getPersistForCustomer());
        response.setSortOrder(definition.getSortOrder());
        response.setOptions(definition.getOptions().stream().map(option -> {
            CustomFieldOptionResponse optionResponse = new CustomFieldOptionResponse();
            optionResponse.setId(option.getId());
            optionResponse.setOptionValue(option.getOptionValue());
            optionResponse.setLabel(option.getLabel());
            optionResponse.setSortOrder(option.getSortOrder());
            return optionResponse;
        }).toList());
        return response;
    }

    private CustomerCustomFieldValueResponse toValueResponse(CustomerCustomFieldValue value) {
        CustomerCustomFieldValueResponse response = new CustomerCustomFieldValueResponse();
        response.setCustomFieldId(value.getCustomField().getId());
        response.setCode(value.getCustomField().getCode());
        response.setLabel(value.getCustomField().getLabel());
        response.setFieldType(value.getCustomField().getFieldType());
        response.setFieldScope(value.getCustomField().getFieldScope());
        response.setPersistForCustomer(value.getCustomField().getPersistForCustomer());
        response.setValue(value.getFieldValue());
        return response;
    }

    private String normalizeNullable(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT).equals(trimmed) ? trimmed : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
