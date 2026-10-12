package com.banca.clientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clientesOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Clientes Service API")
                .description("Gestión de personas y clientes")
                .version("1.0.0"));
    }
}
