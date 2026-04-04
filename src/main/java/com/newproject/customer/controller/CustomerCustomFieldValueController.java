package com.newproject.customer.controller;

import com.newproject.customer.dto.CustomerCustomFieldValueRequest;
import com.newproject.customer.dto.CustomerCustomFieldValueResponse;
import com.newproject.customer.service.CustomFieldService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerCustomFieldValueController {
    private final CustomFieldService customFieldService;

    public CustomerCustomFieldValueController(CustomFieldService customFieldService) {
        this.customFieldService = customFieldService;
    }

    @GetMapping("/{customerId}/custom-fields/values")
    public List<CustomerCustomFieldValueResponse> list(
        @PathVariable Long customerId,
        @RequestParam(value = "scope", required = false) String scope
    ) {
        return customFieldService.listValues(customerId, scope);
    }

    @PutMapping("/{customerId}/custom-fields/values")
    public List<CustomerCustomFieldValueResponse> upsert(
        @PathVariable Long customerId,
        @RequestBody List<CustomerCustomFieldValueRequest> requests
    ) {
        return customFieldService.upsertValues(customerId, requests);
    }
}
