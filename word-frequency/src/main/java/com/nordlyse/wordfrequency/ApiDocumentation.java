package com.nordlyse.wordfrequency;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class ApiDocumentation {

    @Bean
    OpenAPI wordFrequencyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("KeyboardStatisticLogin")
                        .version("0.1.0")
                        .description("""
                                Keeps each account's typing pace and compares a later sentence with that pace.
                                The username is the account id.
                                Most routes need the bearer token from POST /sessions.
                                POST /accounts stays open.
                                """))
                .components(new Components().addSecuritySchemes("bearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("token")
                        .description("Session token from POST /sessions. Send it as Authorization: Bearer.")));
    }
}
