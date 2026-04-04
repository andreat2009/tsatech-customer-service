package com.newproject.customer.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CustomFieldSeedProperties.class)
public class CustomFieldSeedConfig {
}
