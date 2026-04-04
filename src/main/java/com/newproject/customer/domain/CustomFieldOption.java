package com.newproject.customer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "custom_field_option")
public class CustomFieldOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_field_id", nullable = false)
    private CustomFieldDefinition customField;

    @Column(name = "option_value", length = 128, nullable = false)
    private String optionValue;

    @Column(length = 255, nullable = false)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomFieldDefinition getCustomField() {
        return customField;
    }

    public void setCustomField(CustomFieldDefinition customField) {
        this.customField = customField;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
