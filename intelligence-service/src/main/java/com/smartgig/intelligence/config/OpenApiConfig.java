package com.smartgig.intelligence.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "SmartGig Intelligence Service API", version = "v1"))
public class OpenApiConfig {
}

