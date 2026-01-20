package org.example.infra.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.app.usecase.*;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.exception.DeviceInUseException;
import org.example.domain.exception.DeviceNotFoundException;
import org.example.domain.exception.OptimisticLockException;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;
import org.example.infra.rest.dto.CreateDeviceRequest;
import org.example.infra.rest.dto.UpdateDeviceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManageDevicesController.class)
@ActiveProfiles("test")
@DisplayName("ManageDevicesController - Enhanced Integration Tests")
class ManageDevicesControllerEnhancedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateDeviceUseCase createDeviceUseCase;

    @MockBean
    private GetDeviceByIdUseCase getDeviceByIdUseCase;

    @MockBean
    private UpdateDeviceUseCase updateDeviceUseCase;

    @MockBean
    private DeleteDeviceUseCase deleteDeviceUseCase;

    @MockBean
    private ListDevicesUseCase listDevicesUseCase;

    private UUID deviceId;
    private Device device;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        now = LocalDateTime.now();
        device = new Device(
                deviceId,
                "Enhanced Test Device",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                now,
                0L
        );
    }

    @Test
    @DisplayName("POST /v1/devices - Should normalize brand to uppercase")
    void shouldNormalizeBrandToUppercase() throws Exception {
        // Given
        CreateDeviceRequest request = new CreateDeviceRequest(
                "Test Device",
                "apple",  // lowercase
                DeviceStateEnum.AVAILABLE
        );
        Device createdDevice = new Device(deviceId, "Test Device", "APPLE", DeviceStateEnum.AVAILABLE, now, 0L);
        when(createDeviceUseCase.create(any(CreateDeviceRequest.class))).thenReturn(createdDevice);

        // When & Then
        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("APPLE"));
    }

    @Test
    @DisplayName("POST /v1/devices - Should handle special characters in name")
    void shouldHandleSpecialCharactersInName() throws Exception {
        // Given
        CreateDeviceRequest request = new CreateDeviceRequest(
                "Device-Test_123@Special",
                "Samsung",
                DeviceStateEnum.AVAILABLE
        );
        Device createdDevice = new Device(deviceId, "Device-Test_123@Special", "SAMSUNG", DeviceStateEnum.AVAILABLE, now, 0L);
        when(createDeviceUseCase.create(any(CreateDeviceRequest.class))).thenReturn(createdDevice);

        // When & Then
        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Device-Test_123@Special"));
    }

    @Test
    @DisplayName("POST /v1/devices - Should create device with all states")
    void shouldCreateDeviceWithAllStates() throws Exception {
        // Test with IN_USE state
        CreateDeviceRequest request = new CreateDeviceRequest(
                "In Use Device",
                "LG",
                DeviceStateEnum.IN_USE
        );
        Device createdDevice = new Device(deviceId, "In Use Device", "LG", DeviceStateEnum.IN_USE, now, 0L);
        when(createDeviceUseCase.create(any(CreateDeviceRequest.class))).thenReturn(createdDevice);

        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("IN_USE"));
    }

    @Test
    @DisplayName("POST /v1/devices - Should return 400 for empty brand")
    void shouldReturn400ForEmptyBrand() throws Exception {
        // Given
        String invalidJson = """
                {
                    "name": "Test Device",
                    "brand": "",
                    "state": "AVAILABLE"
                }
                """;

        // When & Then
        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/devices/{id} - Should return device with all properties")
    void shouldReturnDeviceWithAllProperties() throws Exception {
        // Given
        Device deviceWithProps = new Device(
                deviceId,
                "Complete Device",
                "APPLE",
                DeviceStateEnum.IN_USE,
                LocalDateTime.of(2025, 1, 1, 12, 0),
                5L
        );
        when(getDeviceByIdUseCase.getById(deviceId)).thenReturn(Optional.of(deviceWithProps));

        // When & Then
        mockMvc.perform(get("/v1/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deviceId.toString()))
                .andExpect(jsonPath("$.name").value("Complete Device"))
                .andExpect(jsonPath("$.brand").value("APPLE"))
                .andExpect(jsonPath("$.state").value("IN_USE"))
                .andExpect(jsonPath("$.version").value(5))
                .andExpect(jsonPath("$.creationTime").exists());
    }

    @Test
    @DisplayName("GET /v1/devices/{id} - Should handle malformed UUID")
    void shouldHandleMalformedUuid() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/devices/{id}", "not-a-valid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /v1/devices/{id} - Should update only name")
    void shouldUpdateOnlyName() throws Exception {
        // Given
        UpdateDeviceRequest request = new UpdateDeviceRequest(
                "Updated Name Only",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                0L
        );
        Device updatedDevice = new Device(deviceId, "Updated Name Only", "SAMSUNG", DeviceStateEnum.AVAILABLE, now, 1L);
        when(updateDeviceUseCase.update(eq(deviceId), any(UpdateDeviceRequest.class)))
                .thenReturn(updatedDevice);

        // When & Then
        mockMvc.perform(put("/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name Only"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    @DisplayName("PUT /v1/devices/{id} - Should handle version increment")
    void shouldHandleVersionIncrement() throws Exception {
        // Given
        UpdateDeviceRequest request = new UpdateDeviceRequest(
                "Device",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                5L  // Current version
        );
        Device updatedDevice = new Device(deviceId, "Device", "BRAND", DeviceStateEnum.AVAILABLE, now, 6L);
        when(updateDeviceUseCase.update(eq(deviceId), any(UpdateDeviceRequest.class)))
                .thenReturn(updatedDevice);

        // When & Then
        mockMvc.perform(put("/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(6));
    }

    @Test
    @DisplayName("PUT /v1/devices/{id} - Should return 409 for wrong version")
    void shouldReturn409ForWrongVersion() throws Exception {
        // Given
        UpdateDeviceRequest request = new UpdateDeviceRequest(
                "Device",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                0L
        );
        when(updateDeviceUseCase.update(eq(deviceId), any(UpdateDeviceRequest.class)))
                .thenThrow(new OptimisticLockException(deviceId));

        // When & Then
        mockMvc.perform(put("/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Concurrent Modification"));
    }

    @Test
    @DisplayName("DELETE /v1/devices/{id} - Should verify no content is returned")
    void shouldVerifyNoContentReturned() throws Exception {
        // Given
        when(deleteDeviceUseCase.delete(deviceId)).thenReturn(DeleteDeviceResult.DELETED);

        // When & Then
        mockMvc.perform(delete("/v1/devices/{id}", deviceId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("DELETE /v1/devices/{id} - Should call delete use case once")
    void shouldCallDeleteUseCaseOnce() throws Exception {
        // Given
        when(deleteDeviceUseCase.delete(deviceId)).thenReturn(DeleteDeviceResult.DELETED);

        // When
        mockMvc.perform(delete("/v1/devices/{id}", deviceId));

        // Then
        verify(deleteDeviceUseCase, times(1)).delete(deviceId);
    }

    @Test
    @DisplayName("GET /v1/devices - Should list devices with filter by brand")
    void shouldListDevicesWithFilterByBrand() throws Exception {
        // Given
        List<Device> devices = List.of(device);
        CursorPage<Device> page = new CursorPage<>(devices, null, 20, false);

        when(listDevicesUseCase.list(any(DeviceFilter.class), any(), eq(20)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/devices")
                        .param("brand", "SAMSUNG")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].brand").value("SAMSUNG"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /v1/devices - Should list devices with filter by state")
    void shouldListDevicesWithFilterByState() throws Exception {
        // Given
        Device inUseDevice = new Device(deviceId, "Device", "Brand", DeviceStateEnum.IN_USE, now, 0L);
        List<Device> devices = List.of(inUseDevice);
        CursorPage<Device> page = new CursorPage<>(devices, null, 20, false);

        when(listDevicesUseCase.list(any(DeviceFilter.class), any(), eq(20)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/devices")
                        .param("state", "IN_USE")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].state").value("IN_USE"));
    }

    @Test
    @DisplayName("GET /v1/devices - Should handle empty result set")
    void shouldHandleEmptyResultSet() throws Exception {
        // Given
        CursorPage<Device> emptyPage = new CursorPage<>(Collections.emptyList(), null, 20, false);

        when(listDevicesUseCase.list(any(DeviceFilter.class), any(), eq(20)))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/v1/devices")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /v1/devices - Should use cursor for pagination")
    void shouldUseCursorForPagination() throws Exception {
        // Given
        UUID cursor = UUID.randomUUID();
        UUID nextCursor = UUID.randomUUID();
        List<Device> devices = List.of(device);
        CursorPage<Device> page = new CursorPage<>(devices, nextCursor, 10, true);

        when(listDevicesUseCase.list(any(DeviceFilter.class), eq(cursor), eq(10)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/devices")
                        .param("cursor", cursor.toString())
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextCursor").value(nextCursor.toString()))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 101, 200})
    @DisplayName("GET /v1/devices - Should validate size parameter bounds")
    void shouldValidateSizeParameterBounds(int invalidSize) throws Exception {
        // Given
        when(listDevicesUseCase.list(any(DeviceFilter.class), any(), eq(invalidSize)))
                .thenThrow(new IllegalArgumentException("Size must be between 1 and 100"));

        // When & Then
        mockMvc.perform(get("/v1/devices")
                        .param("size", String.valueOf(invalidSize)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/devices - Should list with default size when not specified")
    void shouldListWithDefaultSizeWhenNotSpecified() throws Exception {
        // Given
        List<Device> devices = List.of(device);
        CursorPage<Device> page = new CursorPage<>(devices, null, 20, false);

        when(listDevicesUseCase.list(any(DeviceFilter.class), any(), eq(20)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /v1/devices - Should handle combined filters")
    void shouldHandleCombinedFilters() throws Exception {
        // Given
        List<Device> devices = List.of(device);
        CursorPage<Device> page = new CursorPage<>(devices, null, 20, false);

        when(listDevicesUseCase.list(any(DeviceFilter.class), any(), eq(20)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/devices")
                        .param("brand", "SAMSUNG")
                        .param("state", "AVAILABLE")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("Should handle DeviceNotFoundException with 404")
    void shouldHandleDeviceNotFoundExceptionWith404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(getDeviceByIdUseCase.getById(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/v1/devices/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle DeviceInUseException with 409")
    void shouldHandleDeviceInUseExceptionWith409() throws Exception {
        // Given
        when(deleteDeviceUseCase.delete(deviceId))
                .thenReturn(DeleteDeviceResult.IN_USE);

        // When & Then
        mockMvc.perform(delete("/v1/devices/{id}", deviceId))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return proper error response structure")
    void shouldReturnProperErrorResponseStructure() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(updateDeviceUseCase.update(eq(nonExistentId), any(UpdateDeviceRequest.class)))
                .thenThrow(new DeviceNotFoundException(nonExistentId));

        UpdateDeviceRequest request = new UpdateDeviceRequest(
                "Device",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                0L
        );

        // When & Then
        mockMvc.perform(put("/v1/devices/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should accept JSON content type")
    void shouldAcceptJsonContentType() throws Exception {
        // Given
        CreateDeviceRequest request = new CreateDeviceRequest(
                "Device",
                "Brand",
                DeviceStateEnum.AVAILABLE
        );
        when(createDeviceUseCase.create(any(CreateDeviceRequest.class))).thenReturn(device);

        // When & Then
        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return JSON content type")
    void shouldReturnJsonContentType() throws Exception {
        // Given
        when(getDeviceByIdUseCase.getById(deviceId)).thenReturn(Optional.of(device));

        // When & Then
        mockMvc.perform(get("/v1/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
