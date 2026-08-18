package io.github.muaiso.kreditrisiko.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI-Metadaten fuer die interaktive API-Dokumentation (/swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kreditrisikoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Kreditrisiko-Scoring-Engine")
                .description("Trainierbare Klassifikationsmodelle fuer Kreditausfall (ROC/AUC, PR-AUC, KS, Gini)")
                .version("0.1.0"));
    }
}
