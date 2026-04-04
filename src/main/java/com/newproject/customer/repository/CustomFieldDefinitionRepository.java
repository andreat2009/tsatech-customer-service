package com.newproject.customer.repository;

import com.newproject.customer.domain.CustomFieldDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinition, Long> {
    Optional<CustomFieldDefinition> findByCode(String code);

    List<CustomFieldDefinition> findByFieldScopeOrderBySortOrderAscCodeAsc(String fieldScope);

    List<CustomFieldDefinition> findByFieldScopeAndActiveOrderBySortOrderAscCodeAsc(String fieldScope, Boolean active);

    List<CustomFieldDefinition> findByActiveOrderBySortOrderAscCodeAsc(Boolean active);
}
