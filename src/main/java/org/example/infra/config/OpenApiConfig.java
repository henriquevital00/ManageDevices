package org.example.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the ManageDevices API.
 * This configuration provides comprehensive API documentation accessible through Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ManageDevices API")
                        .version("1.0.0")
                        .description("""
                                RESTful API for managing devices with comprehensive features including:
                                - Device CRUD operations with validation
                                - Optimistic locking to prevent concurrent modification conflicts
                                - Cursor-based pagination for efficient data retrieval
                                - Device state management (AVAILABLE, IN_USE, INACTIVE)
                                - Complete audit history tracking for all operations
                                - Global exception handling with detailed error responses
                                
                                ## Architecture
                                This API follows Hexagonal Architecture (Ports and Adapters) and SOLID principles:
                                - **Domain Layer**: Core business logic and entities
                                - **Application Layer**: Use cases and ports
                                - **Infrastructure Layer**: Adapters for REST, persistence, and external services
                                
                                ## Data Consistency
                                - Optimistic locking using version field prevents lost updates
                                - All operations are transactional
                                - History tracking for audit purposes
                                """)
                        .contact(new Contact()
                                .name("ManageDevices Development Team")
                                .email("dev@managedevices.com")
                                .url("https://github.com/managedevices"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.managedevices.com")
                                .description("Production Server")
                ));
    }
}
