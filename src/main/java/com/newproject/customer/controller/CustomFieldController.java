package com.newproject.customer.controller;

import com.newproject.customer.dto.CustomFieldDefinitionRequest;
import com.newproject.customer.dto.CustomFieldDefinitionResponse;
import com.newproject.customer.service.CustomFieldService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers/custom-fields")
public class CustomFieldController {
    private final CustomFieldService customFieldService;

    public CustomFieldController(CustomFieldService customFieldService) {
        this.customFieldService = customFieldService;
    }

    @GetMapping
    public List<CustomFieldDefinitionResponse> list(
        @RequestParam(value = "scope", required = false) String scope,
        @RequestParam(value = "active", required = false) Boolean active
    ) {
        return customFieldService.listDefinitions(scope, active);
    }

    @GetMapping("/{id}")
    public CustomFieldDefinitionResponse get(@PathVariable Long id) {
        return customFieldService.getDefinition(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomFieldDefinitionResponse create(@RequestBody CustomFieldDefinitionRequest request) {
        return customFieldService.createDefinition(request);
    }

    @PutMapping("/{id}")
    public CustomFieldDefinitionResponse update(@PathVariable Long id, @RequestBody CustomFieldDefinitionRequest request) {
        return customFieldService.updateDefinition(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customFieldService.deleteDefinition(id);
    }
}
