package org.example.app.usecase.impl;

import org.example.app.ports.out.DeviceHistoryRepositoryPort;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.enums.OperationTypeEnum;
import org.example.domain.exception.DeviceInUseException;
import org.example.domain.exception.DeviceNotFoundException;
import org.example.infra.rest.dto.UpdateDeviceRequest;
import org.example.infra.rest.dto.PartialUpdateDeviceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateDeviceImpl Unit Tests")
class UpdateDeviceImplTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @Mock
    private DeviceHistoryRepositoryPort deviceHistoryRepositoryPort;

    @InjectMocks
    private UpdateDeviceImpl updateDeviceUseCase;

    private UUID deviceId;
    private Device existingDevice;
    private UpdateDeviceRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        existingDevice = new Device(
                deviceId,
                "Original Name",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                now,
                0L
        );
        
        validUpdateRequest = new UpdateDeviceRequest(
                "Updated Name",
                "apple",
                DeviceStateEnum.IN_USE,
                0L
        );
    }

    @Test
    @DisplayName("Should update device successfully")
    void shouldUpdateDeviceSuccessfully() {
        // Given
        Device updatedDevice = new Device(
                deviceId,
                validUpdateRequest.name(),
                "APPLE",
                validUpdateRequest.state(),
                existingDevice.creationTime(),
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.update(deviceId, validUpdateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.brand()).isEqualTo("APPLE");
        assertThat(result.state()).isEqualTo(DeviceStateEnum.IN_USE);
        
        verify(deviceRepositoryPort).findById(deviceId);
        verify(deviceRepositoryPort).save(any(Device.class));
        verify(deviceHistoryRepositoryPort).save(any(Device.class), eq(OperationTypeEnum.UPDATE));
    }

    @Test
    @DisplayName("Should normalize brand to uppercase")
    void shouldNormalizeBrandToUppercase() {
        // Given
        Device updatedDevice = new Device(
                deviceId,
                validUpdateRequest.name(),
                "APPLE",
                validUpdateRequest.state(),
                existingDevice.creationTime(),
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.update(deviceId, validUpdateRequest);

        // Then
        assertThat(result.brand()).isEqualTo("APPLE");
        verify(deviceRepositoryPort).save(argThat(device -> 
            device.brand().equals("APPLE")
        ));
    }

    @Test
    @DisplayName("Should throw exception when device not found")
    void shouldThrowExceptionWhenDeviceNotFound() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateDeviceUseCase.update(deviceId, validUpdateRequest))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining("Device not found with id: " + deviceId);

        verify(deviceRepositoryPort).findById(deviceId);
        verify(deviceRepositoryPort, never()).save(any());
        verify(deviceHistoryRepositoryPort, never()).save(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when updating name of IN_USE device")
    void shouldThrowExceptionWhenUpdatingNameOfInUseDevice() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Original Name",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                LocalDateTime.now(),
                0L
        );
        
        UpdateDeviceRequest requestWithDifferentName = new UpdateDeviceRequest(
                "Different Name",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));

        // When & Then
        assertThatThrownBy(() -> updateDeviceUseCase.update(deviceId, requestWithDifferentName))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update name while device is IN_USE");

        verify(deviceRepositoryPort).findById(deviceId);
        verify(deviceRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating brand of IN_USE device")
    void shouldThrowExceptionWhenUpdatingBrandOfInUseDevice() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Device Name",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                LocalDateTime.now(),
                0L
        );
        
        UpdateDeviceRequest requestWithDifferentBrand = new UpdateDeviceRequest(
                "Device Name",
                "apple",
                DeviceStateEnum.IN_USE,
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));

        // When & Then
        assertThatThrownBy(() -> updateDeviceUseCase.update(deviceId, requestWithDifferentBrand))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update brand while device is IN_USE");

        verify(deviceRepositoryPort).findById(deviceId);
        verify(deviceRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should allow state change for IN_USE device")
    void shouldAllowStateChangeForInUseDevice() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Device Name",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                LocalDateTime.now(),
                0L
        );
        
        UpdateDeviceRequest requestWithSameNameAndBrand = new UpdateDeviceRequest(
                "Device Name",
                "samsung",
                DeviceStateEnum.AVAILABLE,
                0L
        );
        
        Device updatedDevice = new Device(
                deviceId,
                "Device Name",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                inUseDevice.creationTime(),
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.update(deviceId, requestWithSameNameAndBrand);

        // Then
        assertThat(result.state()).isEqualTo(DeviceStateEnum.AVAILABLE);
        verify(deviceRepositoryPort).save(any(Device.class));
    }

    @Test
    @DisplayName("Should handle null brand gracefully")
    void shouldHandleNullBrand() {
        // Given
        UpdateDeviceRequest requestWithNullBrand = new UpdateDeviceRequest(
                "Updated Name",
                null,
                DeviceStateEnum.AVAILABLE,
                0L
        );
        
        Device updatedDevice = new Device(
                deviceId,
                "Updated Name",
                null,
                DeviceStateEnum.AVAILABLE,
                existingDevice.creationTime(),
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.update(deviceId, requestWithNullBrand);

        // Then
        assertThat(result.brand()).isNull();
    }

    @Test
    @DisplayName("Should handle empty brand gracefully")
    void shouldHandleEmptyBrand() {
        // Given
        UpdateDeviceRequest requestWithEmptyBrand = new UpdateDeviceRequest(
                "Updated Name",
                "",
                DeviceStateEnum.AVAILABLE,
                0L
        );
        
        Device updatedDevice = new Device(
                deviceId,
                "Updated Name",
                "",
                DeviceStateEnum.AVAILABLE,
                existingDevice.creationTime(),
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.update(deviceId, requestWithEmptyBrand);

        // Then
        assertThat(result.brand()).isEmpty();
    }

    @Test
    @DisplayName("Should preserve creation time on update")
    void shouldPreserveCreationTimeOnUpdate() {
        // Given
        LocalDateTime originalCreationTime = existingDevice.creationTime();
        Device updatedDevice = new Device(
                deviceId,
                validUpdateRequest.name(),
                "APPLE",
                validUpdateRequest.state(),
                originalCreationTime,
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.update(deviceId, validUpdateRequest);

        // Then
        assertThat(result.creationTime()).isEqualTo(originalCreationTime);
    }

    @Test
    @DisplayName("Should save device history on update")
    void shouldSaveDeviceHistoryOnUpdate() {
        // Given
        Device updatedDevice = new Device(
                deviceId,
                validUpdateRequest.name(),
                "APPLE",
                validUpdateRequest.state(),
                existingDevice.creationTime(),
                0L
        );
        
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        updateDeviceUseCase.update(deviceId, validUpdateRequest);

        // Then
        verify(deviceHistoryRepositoryPort).save(any(Device.class), eq(OperationTypeEnum.UPDATE));
    }

    @Test
    @DisplayName("Partial Update - Should update only state")
    void partialUpdateShouldUpdateOnlyState() {
        // Given
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.of(DeviceStateEnum.IN_USE),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                existingDevice.name(),
                existingDevice.brand(),
                DeviceStateEnum.IN_USE,
                existingDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        assertThat(result.state()).isEqualTo(DeviceStateEnum.IN_USE);
        assertThat(result.name()).isEqualTo("Original Name");
        assertThat(result.brand()).isEqualTo("SAMSUNG");

        verify(deviceRepositoryPort).save(any(Device.class));
        verify(deviceHistoryRepositoryPort).save(any(Device.class), eq(OperationTypeEnum.UPDATE));
    }

    @Test
    @DisplayName("Partial Update - Should update only name and brand on AVAILABLE device")
    void partialUpdateShouldUpdateNameAndBrandOnAvailableDevice() {
        // Given
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.of("New Name"),
                Optional.of("lg"),
                Optional.empty(),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                "New Name",
                "LG",
                existingDevice.state(),
                existingDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.brand()).isEqualTo("LG");
        assertThat(result.state()).isEqualTo(DeviceStateEnum.AVAILABLE);
    }

    @Test
    @DisplayName("Partial Update - Should throw exception when updating name of IN_USE device")
    void partialUpdateShouldThrowExceptionWhenUpdatingNameOfInUseDevice() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Original Name",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                existingDevice.creationTime(),
                0L
        );

        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.of("Different Name"),
                Optional.empty(),
                Optional.empty(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));

        // When & Then
        assertThatThrownBy(() -> updateDeviceUseCase.partialUpdate(deviceId, request))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update name while device is IN_USE");

        verify(deviceRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Partial Update - Should throw exception when updating brand of IN_USE device")
    void partialUpdateShouldThrowExceptionWhenUpdatingBrandOfInUseDevice() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Device Name",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                existingDevice.creationTime(),
                0L
        );

        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.empty(),
                Optional.of("apple"),
                Optional.empty(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));

        // When & Then
        assertThatThrownBy(() -> updateDeviceUseCase.partialUpdate(deviceId, request))
                .isInstanceOf(DeviceInUseException.class)
                .hasMessageContaining("Cannot update brand while device is IN_USE");

        verify(deviceRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Partial Update - Should allow state change on IN_USE device")
    void partialUpdateShouldAllowStateChangeOnInUseDevice() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Device Name",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                existingDevice.creationTime(),
                0L
        );

        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.of(DeviceStateEnum.AVAILABLE),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                inUseDevice.name(),
                inUseDevice.brand(),
                DeviceStateEnum.AVAILABLE,
                inUseDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        assertThat(result.state()).isEqualTo(DeviceStateEnum.AVAILABLE);
        verify(deviceRepositoryPort).save(any(Device.class));
    }

    @Test
    @DisplayName("Partial Update - Should throw exception when device not found")
    void partialUpdateShouldThrowExceptionWhenDeviceNotFound() {
        // Given
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.of("New Name"),
                Optional.empty(),
                Optional.empty(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateDeviceUseCase.partialUpdate(deviceId, request))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining("Device not found with id: " + deviceId);

        verify(deviceRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Partial Update - Should normalize brand to uppercase")
    void partialUpdateShouldNormalizeBrandToUppercase() {
        // Given
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.empty(),
                Optional.of("sony"),
                Optional.empty(),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                existingDevice.name(),
                "SONY",
                existingDevice.state(),
                existingDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        assertThat(result.brand()).isEqualTo("SONY");
    }

    @Test
    @DisplayName("Partial Update - Should preserve creation time")
    void partialUpdateShouldPreserveCreationTime() {
        // Given
        LocalDateTime originalCreationTime = existingDevice.creationTime();

        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.of("New Name"),
                Optional.empty(),
                Optional.empty(),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                "New Name",
                existingDevice.brand(),
                existingDevice.state(),
                originalCreationTime,
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        assertThat(result.creationTime()).isEqualTo(originalCreationTime);
    }

    @Test
    @DisplayName("Partial Update - Should save device history")
    void partialUpdateShouldSaveDeviceHistory() {
        // Given
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.of("New Name"),
                Optional.empty(),
                Optional.empty(),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                "New Name",
                existingDevice.brand(),
                existingDevice.state(),
                existingDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        verify(deviceHistoryRepositoryPort).save(any(Device.class), eq(OperationTypeEnum.UPDATE));
    }

    @Test
    @DisplayName("Partial Update - Should handle empty optional fields (keep existing)")
    void partialUpdateShouldHandleEmptyOptionalFields() {
        // Given
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                existingDevice.name(),
                existingDevice.brand(),
                existingDevice.state(),
                existingDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        assertThat(result.name()).isEqualTo("Original Name");
        assertThat(result.brand()).isEqualTo("SAMSUNG");
        assertThat(result.state()).isEqualTo(DeviceStateEnum.AVAILABLE);
    }

    @Test
    @DisplayName("Partial Update - Should update all fields when all provided")
    void partialUpdateShouldUpdateAllFieldsWhenAllProvided() {
        // Given
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.of("Updated Name"),
                Optional.of("lg"),
                Optional.of(DeviceStateEnum.INACTIVE),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                "Updated Name",
                "LG",
                DeviceStateEnum.INACTIVE,
                existingDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.brand()).isEqualTo("LG");
        assertThat(result.state()).isEqualTo(DeviceStateEnum.INACTIVE);
    }

    @Test
    @DisplayName("Partial Update - Should allow updating name and brand when same as current on IN_USE device")
    void partialUpdateShouldAllowSameNameAndBrandOnInUseDevice() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Device Name",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                existingDevice.creationTime(),
                0L
        );

        // Request with same name and brand
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.of("Device Name"),
                Optional.of("samsung"),
                Optional.of(DeviceStateEnum.AVAILABLE),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                "Device Name",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                inUseDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        Device result = updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Device Name");
        assertThat(result.brand()).isEqualTo("SAMSUNG");
        verify(deviceRepositoryPort).save(any(Device.class));
    }

    @Test
    @DisplayName("Partial Update - Should update version")
    void partialUpdateShouldUpdateVersion() {
        // Given
        PartialUpdateDeviceRequest request = new PartialUpdateDeviceRequest(
                Optional.of("New Name"),
                Optional.empty(),
                Optional.empty(),
                0L
        );

        Device updatedDevice = new Device(
                deviceId,
                "New Name",
                existingDevice.brand(),
                existingDevice.state(),
                existingDevice.creationTime(),
                0L
        );

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(updatedDevice);

        // When
        updateDeviceUseCase.partialUpdate(deviceId, request);

        // Then
        verify(deviceRepositoryPort).save(argThat(device -> device.version() == 0L));
    }
}
