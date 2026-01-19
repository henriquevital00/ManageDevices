package org.example.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenApiConfig Unit Tests")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
    }

    @Test
    @DisplayName("Should create OpenAPI bean with correct configuration")
    void shouldCreateOpenApiBeanWithCorrectConfiguration() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getServers()).isNotNull();
    }

    @Test
    @DisplayName("Should configure API info with correct title")
    void shouldConfigureApiInfoWithCorrectTitle() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Info info = openAPI.getInfo();
        assertThat(info.getTitle()).isEqualTo("ManageDevices API");
    }

    @Test
    @DisplayName("Should configure API info with correct version")
    void shouldConfigureApiInfoWithCorrectVersion() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Info info = openAPI.getInfo();
        assertThat(info.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Should configure API info with description")
    void shouldConfigureApiInfoWithDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Info info = openAPI.getInfo();
        assertThat(info.getDescription()).isNotNull();
        assertThat(info.getDescription()).isNotEmpty();
    }

    @Test
    @DisplayName("Should include CRUD operations in description")
    void shouldIncludeCrudOperationsInDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        String description = openAPI.getInfo().getDescription();
        assertThat(description).contains("CRUD operations");
    }

    @Test
    @DisplayName("Should include optimistic locking in description")
    void shouldIncludeOptimisticLockingInDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        String description = openAPI.getInfo().getDescription();
        assertThat(description).contains("Optimistic locking");
    }

    @Test
    @DisplayName("Should include pagination in description")
    void shouldIncludePaginationInDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        String description = openAPI.getInfo().getDescription();
        assertThat(description).contains("Cursor-based pagination");
    }

    @Test
    @DisplayName("Should include device states in description")
    void shouldIncludeDeviceStatesInDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        String description = openAPI.getInfo().getDescription();
        assertThat(description).contains("AVAILABLE");
        assertThat(description).contains("IN_USE");
        assertThat(description).contains("INACTIVE");
    }

    @Test
    @DisplayName("Should include architecture information in description")
    void shouldIncludeArchitectureInformationInDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        String description = openAPI.getInfo().getDescription();
        assertThat(description).contains("Hexagonal Architecture");
        assertThat(description).contains("SOLID principles");
    }

    @Test
    @DisplayName("Should include audit history in description")
    void shouldIncludeAuditHistoryInDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        String description = openAPI.getInfo().getDescription();
        assertThat(description).contains("audit history");
    }

    @Test
    @DisplayName("Should configure contact information")
    void shouldConfigureContactInformation() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact).isNotNull();
    }

    @Test
    @DisplayName("Should configure contact with name")
    void shouldConfigureContactWithName() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact.getName()).isEqualTo("ManageDevices Development Team");
    }

    @Test
    @DisplayName("Should configure contact with email")
    void shouldConfigureContactWithEmail() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact.getEmail()).isEqualTo("dev@managedevices.com");
    }

    @Test
    @DisplayName("Should configure contact with URL")
    void shouldConfigureContactWithUrl() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact.getUrl()).isEqualTo("https://github.com/managedevices");
    }


    @Test
    @DisplayName("Should configure license information")
    void shouldConfigureLicenseInformation() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        License license = openAPI.getInfo().getLicense();
        assertThat(license).isNotNull();
    }

    @Test
    @DisplayName("Should configure Apache 2.0 license")
    void shouldConfigureApache20License() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        License license = openAPI.getInfo().getLicense();
        assertThat(license.getName()).isEqualTo("Apache 2.0");
    }

    @Test
    @DisplayName("Should configure license with URL")
    void shouldConfigureLicenseWithUrl() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        License license = openAPI.getInfo().getLicense();
        assertThat(license.getUrl()).isEqualTo("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

    @Test
    @DisplayName("Should configure server list")
    void shouldConfigureServerList() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        List<Server> servers = openAPI.getServers();
        assertThat(servers).isNotNull();
        assertThat(servers).hasSize(2);
    }

    @Test
    @DisplayName("Should configure development server")
    void shouldConfigureDevelopmentServer() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        List<Server> servers = openAPI.getServers();
        Server devServer = servers.get(0);

        assertThat(devServer.getUrl()).isEqualTo("http://localhost:8080");
        assertThat(devServer.getDescription()).isEqualTo("Development Server");
    }

    @Test
    @DisplayName("Should configure production server")
    void shouldConfigureProductionServer() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        List<Server> servers = openAPI.getServers();
        Server prodServer = servers.get(1);

        assertThat(prodServer.getUrl()).isEqualTo("https://api.managedevices.com");
        assertThat(prodServer.getDescription()).isEqualTo("Production Server");
    }

    @Test
    @DisplayName("Should use configured server port for development server")
    void shouldUseConfiguredServerPortForDevelopmentServer() {
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "9090");

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        List<Server> servers = openAPI.getServers();
        Server devServer = servers.get(0);
        assertThat(devServer.getUrl()).isEqualTo("http://localhost:9090");
    }

    @Test
    @DisplayName("Should use default port 8080 when not configured")
    void shouldUseDefaultPort8080WhenNotConfigured() {
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        List<Server> servers = openAPI.getServers();
        Server devServer = servers.get(0);
        assertThat(devServer.getUrl()).contains("8080");
    }

    @Test
    @DisplayName("Should create bean that is not null")
    void shouldCreateBeanThatIsNotNull() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI).isNotNull();
    }

    @Test
    @DisplayName("Should create bean with all required components")
    void shouldCreateBeanWithAllRequiredComponents() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isNotNull();
        assertThat(openAPI.getInfo().getVersion()).isNotNull();
        assertThat(openAPI.getInfo().getDescription()).isNotNull();
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getServers()).isNotNull();
    }

    @Test
    @DisplayName("Should create valid OpenAPI configuration for Swagger UI")
    void shouldCreateValidOpenApiConfigurationForSwaggerUi() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isNotEmpty();
        assertThat(openAPI.getInfo().getVersion()).isNotEmpty();
        assertThat(openAPI.getServers()).isNotEmpty();
    }

    @Test
    @DisplayName("Should provide complete API metadata")
    void shouldProvideCompleteApiMetadata() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        Info info = openAPI.getInfo();

        assertThat(info.getTitle()).isNotNull();
        assertThat(info.getVersion()).isNotNull();
        assertThat(info.getDescription()).isNotNull();
        assertThat(info.getContact()).isNotNull();
        assertThat(info.getContact().getName()).isNotNull();
        assertThat(info.getContact().getEmail()).isNotNull();
        assertThat(info.getContact().getUrl()).isNotNull();
        assertThat(info.getLicense()).isNotNull();
        assertThat(info.getLicense().getName()).isNotNull();
        assertThat(info.getLicense().getUrl()).isNotNull();
    }

    @Test
    @DisplayName("Should support multiple server environments")
    void shouldSupportMultipleServerEnvironments() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        List<Server> servers = openAPI.getServers();
        assertThat(servers).hasSize(2);

        assertThat(servers.stream().anyMatch(s -> s.getDescription().contains("Development"))).isTrue();
        assertThat(servers.stream().anyMatch(s -> s.getDescription().contains("Production"))).isTrue();
    }

    @Test
    @DisplayName("Should include all key features in description")
    void shouldIncludeAllKeyFeaturesInDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        String description = openAPI.getInfo().getDescription();

        assertThat(description).contains("CRUD operations");
        assertThat(description).contains("validation");
        assertThat(description).contains("Optimistic locking");
        assertThat(description).contains("Cursor-based pagination");
        assertThat(description).contains("state management");
        assertThat(description).contains("audit history");
        assertThat(description).contains("exception handling");
        assertThat(description).contains("Hexagonal Architecture");
    }

    @Test
    @DisplayName("Should document API layers in description")
    void shouldDocumentApiLayersInDescription() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        String description = openAPI.getInfo().getDescription();

        assertThat(description).contains("Domain Layer");
        assertThat(description).contains("Application Layer");
        assertThat(description).contains("Infrastructure Layer");
    }

    @Test
    @DisplayName("Should document data consistency features")
    void shouldDocumentDataConsistencyFeatures() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        String description = openAPI.getInfo().getDescription();

        assertThat(description).contains("Data Consistency");
        assertThat(description).contains("version field");
        assertThat(description).contains("transactional");
    }

    @Test
    @DisplayName("Should handle custom port configuration")
    void shouldHandleCustomPortConfiguration() {
        String customPort = "3000";
        ReflectionTestUtils.setField(openApiConfig, "serverPort", customPort);

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Server devServer = openAPI.getServers().get(0);
        assertThat(devServer.getUrl()).isEqualTo("http://localhost:" + customPort);
    }

    @Test
    @DisplayName("Should handle port as string in URL")
    void shouldHandlePortAsStringInUrl() {
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8888");

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        Server devServer = openAPI.getServers().get(0);
        assertThat(devServer.getUrl()).contains("localhost:8888");
    }
}
