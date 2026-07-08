package com.onder.garage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI garageOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Garage Systems API")
                        .description("Parking garage management API with slot allocation and buffer rules")
                        .version("1.0.0"));
    }
}
