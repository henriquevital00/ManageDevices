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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManageDevicesController.class)
@ActiveProfiles("test")
@DisplayName("ManageDevicesController Unit Tests")
class ManageDevicesControllerTest {

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
    private CreateDeviceRequest createRequest;
    private UpdateDeviceRequest updateRequest;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        device = new Device(
                deviceId,
                "Test Device",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                now,
                0L
        );
        
        createRequest = new CreateDeviceRequest(
                "New Device",
                "apple",
                DeviceStateEnum.AVAILABLE
        );
        
        updateRequest = new UpdateDeviceRequest(
                "Updated Device",
                "lg",
                DeviceStateEnum.IN_USE,
                0L
        );
    }

    @Test
    @DisplayName("POST /v1/devices - Should create device successfully")
    void shouldCreateDeviceSuccessfully() throws Exception {
        // Given
        when(createDeviceUseCase.create(any(CreateDeviceRequest.class))).thenReturn(device);

        // When & Then
        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(deviceId.toString()))
                .andExpect(jsonPath("$.name").value(device.name()))
                .andExpect(jsonPath("$.brand").value(device.brand()))
                .andExpect(jsonPath("$.state").value(device.state().toString()));

        verify(createDeviceUseCase).create(any(CreateDeviceRequest.class));
    }

    @Test
    @DisplayName("POST /v1/devices - Should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        // Given
        CreateDeviceRequest invalidRequest = new CreateDeviceRequest(
                "",
                "brand",
                DeviceStateEnum.AVAILABLE
        );

        // When & Then
        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/devices - Should return 400 when state is null")
    void shouldReturn400WhenStateIsNull() throws Exception {
        // Given
        CreateDeviceRequest invalidRequest = new CreateDeviceRequest(
                "Device Name",
                "brand",
                null
        );

        // When & Then
        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/devices/{id} - Should return device when found")
    void shouldReturnDeviceWhenFound() throws Exception {
        // Given
        when(getDeviceByIdUseCase.getById(deviceId)).thenReturn(Optional.of(device));

        // When & Then
        mockMvc.perform(get("/v1/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deviceId.toString()))
                .andExpect(jsonPath("$.name").value(device.name()))
                .andExpect(jsonPath("$.brand").value(device.brand()))
                .andExpect(jsonPath("$.state").value(device.state().toString()))
                .andExpect(jsonPath("$.version").value(device.version()));

        verify(getDeviceByIdUseCase).getById(deviceId);
    }

    @Test
    @DisplayName("GET /v1/devices/{id} - Should return 404 when device not found")
    void shouldReturn404WhenDeviceNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(getDeviceByIdUseCase.getById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/v1/devices/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(getDeviceByIdUseCase).getById(nonExistentId);
    }

    @Test
    @DisplayName("PUT /v1/devices/{id} - Should update device successfully")
    void shouldUpdateDeviceSuccessfully() throws Exception {
        // Given
        Device updatedDevice = new Device(
                deviceId,
                updateRequest.name(),
                updateRequest.brand().toUpperCase(),
                updateRequest.state(),
                device.creationTime(),
                1L
        );
        when(updateDeviceUseCase.update(eq(deviceId), any(UpdateDeviceRequest.class)))
                .thenReturn(updatedDevice);

        // When & Then
        mockMvc.perform(put("/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deviceId.toString()))
                .andExpect(jsonPath("$.name").value(updateRequest.name()))
                .andExpect(jsonPath("$.version").value(1L));

        verify(updateDeviceUseCase).update(eq(deviceId), any(UpdateDeviceRequest.class));
    }

    @Test
    @DisplayName("PUT /v1/devices/{id} - Should return 404 when device not found")
    void shouldReturn404OnUpdateWhenDeviceNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(updateDeviceUseCase.update(eq(nonExistentId), any(UpdateDeviceRequest.class)))
                .thenThrow(new DeviceNotFoundException(nonExistentId));

        // When & Then
        mockMvc.perform(put("/v1/devices/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /v1/devices/{id} - Should return 409 on optimistic lock exception")
    void shouldReturn409OnOptimisticLockException() throws Exception {
        // Given
        when(updateDeviceUseCase.update(eq(deviceId), any(UpdateDeviceRequest.class)))
                .thenThrow(new OptimisticLockException(deviceId));

        // When & Then
        mockMvc.perform(put("/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /v1/devices/{id} - Should return 409 when updating IN_USE device")
    void shouldReturn409WhenUpdatingInUseDevice() throws Exception {
        // Given
        when(updateDeviceUseCase.update(eq(deviceId), any(UpdateDeviceRequest.class)))
                .thenThrow(new DeviceInUseException("name", "update"));

        // When & Then
        mockMvc.perform(put("/v1/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /v1/devices/{id} - Should delete device successfully")
    void shouldDeleteDeviceSuccessfully() throws Exception {
        // Given
        when(deleteDeviceUseCase.delete(deviceId)).thenReturn(DeleteDeviceResult.DELETED);

        // When & Then
        mockMvc.perform(delete("/v1/devices/{id}", deviceId))
                .andExpect(status().isNoContent());

        verify(deleteDeviceUseCase).delete(deviceId);
    }

    @Test
    @DisplayName("DELETE /v1/devices/{id} - Should return 404 when device not found")
    void shouldReturn404OnDeleteWhenDeviceNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deleteDeviceUseCase.delete(nonExistentId))
                .thenThrow(new DeviceNotFoundException(nonExistentId));

        // When & Then
        mockMvc.perform(delete("/v1/devices/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /v1/devices/{id} - Should return 409 when deleting IN_USE device")
    void shouldReturn409WhenDeletingInUseDevice() throws Exception {
        // Given
        when(deleteDeviceUseCase.delete(deviceId))
                .thenThrow(new DeviceInUseException("Cannot delete device while it is IN_USE"));

        // When & Then
        mockMvc.perform(delete("/v1/devices/{id}", deviceId))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /v1/devices - Should list devices with pagination")
    void shouldListDevicesWithPagination() throws Exception {
        // Given
        List<Device> devices = List.of(device);
        UUID nextCursor = UUID.randomUUID();
        CursorPage<Device> page = new CursorPage<>(devices, nextCursor, 20, true);

        when(listDevicesUseCase.list(any(DeviceFilter.class), any(), eq(20)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/v1/devices")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(deviceId.toString()))
                .andExpect(jsonPath("$.nextCursor").value(nextCursor.toString()))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    @DisplayName("GET /v1/devices - Should list devices with filter")
    void shouldListDevicesWithFilter() throws Exception {
        // Given
        DeviceFilter filter = new DeviceFilter("SAMSUNG", DeviceStateEnum.AVAILABLE, null);
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
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].brand").value("SAMSUNG"))
                .andExpect(jsonPath("$.content[0].state").value("AVAILABLE"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /v1/devices - Should return 400 for invalid size")
    void shouldReturn400ForInvalidSize() throws Exception {
        // Given
        when(listDevicesUseCase.list(any(DeviceFilter.class), any(), eq(0)))
                .thenThrow(new IllegalArgumentException("Size must be between 1 and 100"));

        // When & Then
        mockMvc.perform(get("/v1/devices")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
