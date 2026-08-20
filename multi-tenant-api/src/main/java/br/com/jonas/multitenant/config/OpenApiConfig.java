package br.com.jonas.multitenant.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI multiTenantPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Multi-Tenant Platform API")
                        .description("REST API para gerenciamento de Pessoas e Beneficiários em uma arquitetura multi-tenant.")
                        .version("v1"));
    }
}
