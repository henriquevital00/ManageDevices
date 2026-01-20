package org.example.app.usecase.impl;

import org.example.app.ports.out.DeviceHistoryRepositoryPort;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.enums.OperationTypeEnum;
import org.example.infra.rest.dto.CreateDeviceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateDeviceImpl Unit Tests")
class CreateDeviceImplTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @Mock
    private DeviceHistoryRepositoryPort deviceHistoryRepositoryPort;

    @InjectMocks
    private CreateDeviceImpl createDeviceUseCase;

    private CreateDeviceRequest validRequest;
    private Device expectedDevice;

    @BeforeEach
    void setUp() {
        validRequest = new CreateDeviceRequest(
                "Temperature Sensor",
                "samsung",
                DeviceStateEnum.AVAILABLE
        );

        UUID deviceId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        expectedDevice = new Device(
                deviceId,
                "Temperature Sensor",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                now,
                0L
        );
    }

    @Test
    @DisplayName("Should create device successfully with normalized brand")
    void shouldCreateDeviceSuccessfully() {
        // Arrange
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(expectedDevice);

        // Act
        Device result = createDeviceUseCase.create(validRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(validRequest.name());
        assertThat(result.brand()).isEqualTo("SAMSUNG"); // Brand normalized to uppercase
        assertThat(result.state()).isEqualTo(validRequest.state());
        assertThat(result.version()).isEqualTo(0L);

        verify(deviceRepositoryPort, times(1)).save(any(Device.class));
        verify(deviceHistoryRepositoryPort, times(1)).save(any(Device.class), eq(OperationTypeEnum.CREATION));
    }

    @Test
    @DisplayName("Should normalize brand to uppercase")
    void shouldNormalizeBrandToUppercase() {
        // Arrange
        CreateDeviceRequest requestWithLowercaseBrand = new CreateDeviceRequest(
                "Device Name",
                "apple",
                DeviceStateEnum.AVAILABLE
        );

        Device deviceWithUppercaseBrand = new Device(
                UUID.randomUUID(),
                "Device Name",
                "APPLE",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );

        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(deviceWithUppercaseBrand);

        // Act
        Device result = createDeviceUseCase.create(requestWithLowercaseBrand);

        // Assert
        assertThat(result.brand()).isEqualTo("APPLE");
        verify(deviceRepositoryPort).save(argThat(device ->
                device.brand().equals("APPLE")
        ));
    }

    @Test
    @DisplayName("Should handle null brand")
    void shouldHandleNullBrand() {
        // Arrange
        CreateDeviceRequest requestWithNullBrand = new CreateDeviceRequest(
                "Device Name",
                null,
                DeviceStateEnum.AVAILABLE
        );

        Device deviceWithNullBrand = new Device(
                UUID.randomUUID(),
                "Device Name",
                null,
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );

        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(deviceWithNullBrand);

        // Act
        Device result = createDeviceUseCase.create(requestWithNullBrand);

        // Assert
        assertThat(result.brand()).isNull();
    }

    @Test
    @DisplayName("Should handle empty brand")
    void shouldHandleEmptyBrand() {
        // Arrange
        CreateDeviceRequest requestWithEmptyBrand = new CreateDeviceRequest(
                "Device Name",
                "",
                DeviceStateEnum.AVAILABLE
        );

        Device deviceWithEmptyBrand = new Device(
                UUID.randomUUID(),
                "Device Name",
                "",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );

        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(deviceWithEmptyBrand);

        // Act
        Device result = createDeviceUseCase.create(requestWithEmptyBrand);

        // Assert
        assertThat(result.brand()).isEmpty();
    }

    @Test
    @DisplayName("Should create device with IN_USE state")
    void shouldCreateDeviceWithInUseState() {
        // Arrange
        CreateDeviceRequest inUseRequest = new CreateDeviceRequest(
                "Active Device",
                "LG",
                DeviceStateEnum.IN_USE
        );

        Device inUseDevice = new Device(
                UUID.randomUUID(),
                "Active Device",
                "LG",
                DeviceStateEnum.IN_USE,
                LocalDateTime.now(),
                0L
        );

        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(inUseDevice);

        // Act
        Device result = createDeviceUseCase.create(inUseRequest);

        // Assert
        assertThat(result.state()).isEqualTo(DeviceStateEnum.IN_USE);
    }

    @Test
    @DisplayName("Should save device history on creation")
    void shouldSaveDeviceHistory() {
        // Arrange
        when(deviceRepositoryPort.save(any(Device.class))).thenReturn(expectedDevice);

        // Act
        createDeviceUseCase.create(validRequest);

        // Assert
        verify(deviceHistoryRepositoryPort, times(1))
                .save(any(Device.class), eq(OperationTypeEnum.CREATION));
    }
}
