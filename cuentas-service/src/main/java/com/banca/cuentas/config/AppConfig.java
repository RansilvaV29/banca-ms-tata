package com.banca.cuentas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public OpenAPI cuentasOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Cuentas Service API")
                .description("Gestión de cuentas, movimientos y reportes de estado de cuenta")
                .version("1.0.0"));
    }
}
