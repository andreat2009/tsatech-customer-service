package com.newproject.customer.config;

import com.newproject.customer.domain.CustomFieldDefinition;
import com.newproject.customer.domain.CustomFieldOption;
import com.newproject.customer.repository.CustomFieldDefinitionRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CustomFieldSeedRunner implements ApplicationRunner {
    private final CustomFieldDefinitionRepository definitionRepository;
    private final CustomFieldSeedProperties properties;

    public CustomFieldSeedRunner(CustomFieldDefinitionRepository definitionRepository, CustomFieldSeedProperties properties) {
        this.definitionRepository = definitionRepository;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (definitionRepository.findByCode("DELIVERY_SLOT").isPresent()) {
            return;
        }

        CustomFieldDefinition definition = new CustomFieldDefinition();
        definition.setCode("DELIVERY_SLOT");
        definition.setLabel("Preferred delivery slot");
        definition.setPlaceholder("Choose a preferred delivery window");
        definition.setHelpText("Used only to prioritize fulfillment during checkout.");
        definition.setFieldType("SELECT");
        definition.setFieldScope("CHECKOUT");
        definition.setRequired(false);
        definition.setActive(true);
        definition.setPersistForCustomer(false);
        definition.setSortOrder(10);

        CustomFieldOption morning = new CustomFieldOption();
        morning.setOptionValue("morning");
        morning.setLabel("Morning (08:00 - 12:00)");
        morning.setSortOrder(10);

        CustomFieldOption afternoon = new CustomFieldOption();
        afternoon.setOptionValue("afternoon");
        afternoon.setLabel("Afternoon (12:00 - 18:00)");
        afternoon.setSortOrder(20);

        CustomFieldOption evening = new CustomFieldOption();
        evening.setOptionValue("evening");
        evening.setLabel("Evening (18:00 - 21:00)");
        evening.setSortOrder(30);

        definition.setOptions(List.of(morning, afternoon, evening));
        definitionRepository.save(definition);
    }
}
