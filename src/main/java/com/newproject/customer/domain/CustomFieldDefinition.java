package com.newproject.customer.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "custom_field_definition")
public class CustomFieldDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false, unique = true)
    private String code;

    @Column(length = 255, nullable = false)
    private String label;

    @Column(length = 255)
    private String placeholder;

    @Column(name = "help_text", length = 1000)
    private String helpText;

    @Column(name = "field_type", length = 32, nullable = false)
    private String fieldType;

    @Column(name = "field_scope", length = 32, nullable = false)
    private String fieldScope;

    @Column(nullable = false)
    private Boolean required;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "persist_for_customer", nullable = false)
    private Boolean persistForCustomer;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @OneToMany(mappedBy = "customField", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<CustomFieldOption> options = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldScope() {
        return fieldScope;
    }

    public void setFieldScope(String fieldScope) {
        this.fieldScope = fieldScope;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getPersistForCustomer() {
        return persistForCustomer;
    }

    public void setPersistForCustomer(Boolean persistForCustomer) {
        this.persistForCustomer = persistForCustomer;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<CustomFieldOption> getOptions() {
        return options;
    }

    public void setOptions(List<CustomFieldOption> options) {
        this.options.clear();
        if (options == null) {
            return;
        }
        for (CustomFieldOption option : options) {
            option.setCustomField(this);
            this.options.add(option);
        }
    }
}
