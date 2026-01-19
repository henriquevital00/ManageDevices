package org.example.app.usecase.impl;

import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDeviceByIdImpl Unit Tests")
class GetDeviceByIdImplTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @InjectMocks
    private GetDeviceByIdImpl getDeviceByIdUseCase;

    private UUID deviceId;
    private Device device;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        device = new Device(
                deviceId,
                "Test Device",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );
    }

    @Test
    @DisplayName("Should return device when device exists")
    void shouldReturnDeviceWhenDeviceExists() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(device);
        verify(deviceRepositoryPort).findById(deviceId);
    }

    @Test
    @DisplayName("Should return empty Optional when device does not exist")
    void shouldReturnEmptyOptionalWhenDeviceDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(deviceRepositoryPort).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should retrieve correct device by ID")
    void shouldRetrieveCorrectDeviceById() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().id()).isEqualTo(deviceId);
        assertThat(result.get().name()).isEqualTo("Test Device");
    }

    @Test
    @DisplayName("Should preserve device properties when retrieving")
    void shouldPreserveDevicePropertiesWhenRetrieving() {
        // Given
        LocalDateTime creationTime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        Device deviceWithProperties = new Device(
                deviceId,
                "My Device",
                "LG",
                DeviceStateEnum.IN_USE,
                creationTime,
                5L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(deviceWithProperties));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("My Device");
        assertThat(result.get().brand()).isEqualTo("LG");
        assertThat(result.get().state()).isEqualTo(DeviceStateEnum.IN_USE);
        assertThat(result.get().creationTime()).isEqualTo(creationTime);
        assertThat(result.get().version()).isEqualTo(5L);
    }

    @ParameterizedTest
    @EnumSource(DeviceStateEnum.class)
    @DisplayName("Should retrieve device for any state")
    void shouldRetrieveDeviceForAnyState(DeviceStateEnum state) {
        // Given
        Device deviceWithState = new Device(deviceId, "Device", "Brand", state, LocalDateTime.now(), 0L);
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(deviceWithState));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().state()).isEqualTo(state);
    }

    @Test
    @DisplayName("Should handle AVAILABLE device state")
    void shouldHandleAvailableDeviceState() {
        // Given
        Device availableDevice = new Device(
                deviceId,
                "Available Device",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(availableDevice));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().state()).isEqualTo(DeviceStateEnum.AVAILABLE);
    }

    @Test
    @DisplayName("Should handle IN_USE device state")
    void shouldHandleInUseDeviceState() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "In Use Device",
                "Brand",
                DeviceStateEnum.IN_USE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().state()).isEqualTo(DeviceStateEnum.IN_USE);
    }

    @Test
    @DisplayName("Should handle INACTIVE device state")
    void shouldHandleInactiveDeviceState() {
        // Given
        Device inactiveDevice = new Device(
                deviceId,
                "Inactive Device",
                "Brand",
                DeviceStateEnum.INACTIVE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inactiveDevice));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().state()).isEqualTo(DeviceStateEnum.INACTIVE);
    }

    @Test
    @DisplayName("Should call repository findById with correct ID")
    void shouldCallRepositoryFindByIdWithCorrectId() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        getDeviceByIdUseCase.getById(deviceId);

        // Then
        verify(deviceRepositoryPort).findById(deviceId);
    }

    @Test
    @DisplayName("Should return Optional containing device details")
    void shouldReturnOptionalContainingDeviceDetails() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(Device.class);
    }

    @Test
    @DisplayName("Should handle multiple consecutive retrievals")
    void shouldHandleMultipleConsecutiveRetrievals() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        Optional<Device> result1 = getDeviceByIdUseCase.getById(deviceId);
        Optional<Device> result2 = getDeviceByIdUseCase.getById(deviceId);
        Optional<Device> result3 = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result3).isPresent();
        assertThat(result1.get()).isEqualTo(result2.get()).isEqualTo(result3.get());
        verify(deviceRepositoryPort, times(3)).findById(deviceId);
    }

    @Test
    @DisplayName("Should handle device with high version number")
    void shouldHandleDeviceWithHighVersionNumber() {
        // Given
        Device deviceWithHighVersion = new Device(
                deviceId,
                "Device",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                999L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(deviceWithHighVersion));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().version()).isEqualTo(999L);
    }

    @Test
    @DisplayName("Should handle device with old creation timestamp")
    void shouldHandleDeviceWithOldCreationTimestamp() {
        // Given
        LocalDateTime oldTimestamp = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
        Device oldDevice = new Device(
                deviceId,
                "Old Device",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                oldTimestamp,
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(oldDevice));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().creationTime()).isEqualTo(oldTimestamp);
    }

    @Test
    @DisplayName("Should handle device with special characters in name")
    void shouldHandleDeviceWithSpecialCharactersInName() {
        // Given
        Device deviceWithSpecialChars = new Device(
                deviceId,
                "Device-123_Test@Special",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(deviceWithSpecialChars));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Device-123_Test@Special");
    }

    @Test
    @DisplayName("Should not throw exception on missing device")
    void shouldNotThrowExceptionOnMissingDevice() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatCode(() -> getDeviceByIdUseCase.getById(nonExistentId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should return same device object from repository")
    void shouldReturnSameDeviceObjectFromRepository() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result.get()).isSameAs(device);
    }

    @Test
    @DisplayName("Should handle null UUID gracefully")
    void shouldHandleNullUuidGracefully() {
        // Given
        when(deviceRepositoryPort.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve device with minimum version")
    void shouldRetrieveDeviceWithMinimumVersion() {
        // Given
        Device deviceMinVersion = new Device(
                deviceId,
                "Device",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(deviceMinVersion));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().version()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "IN_USE", "INACTIVE"})
    @DisplayName("Should retrieve device for all valid states as strings")
    void shouldRetrieveDeviceForAllValidStatesAsStrings(String stateString) {
        // Given
        DeviceStateEnum state = DeviceStateEnum.valueOf(stateString);
        Device deviceWithState = new Device(deviceId, "Device", "Brand", state, LocalDateTime.now(), 0L);
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(deviceWithState));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().state()).isEqualTo(state);
    }

    @Test
    @DisplayName("Should verify repository is called exactly once")
    void shouldVerifyRepositoryIsCalledExactlyOnce() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        getDeviceByIdUseCase.getById(deviceId);

        // Then
        verify(deviceRepositoryPort).findById(deviceId);
    }

    @Test
    @DisplayName("Should handle different device IDs correctly")
    void shouldHandleDifferentDeviceIdsCorrectly() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Device device1 = new Device(id1, "Device 1", "Brand", DeviceStateEnum.AVAILABLE, LocalDateTime.now(), 0L);
        Device device2 = new Device(id2, "Device 2", "Brand", DeviceStateEnum.AVAILABLE, LocalDateTime.now(), 0L);

        when(deviceRepositoryPort.findById(id1)).thenReturn(Optional.of(device1));
        when(deviceRepositoryPort.findById(id2)).thenReturn(Optional.of(device2));

        // When
        Optional<Device> result1 = getDeviceByIdUseCase.getById(id1);
        Optional<Device> result2 = getDeviceByIdUseCase.getById(id2);

        // Then
        assertThat(result1.get().id()).isEqualTo(id1);
        assertThat(result2.get().id()).isEqualTo(id2);
        assertThat(result1.get()).isNotEqualTo(result2.get());
    }

    @Test
    @DisplayName("Should return Optional with device name intact")
    void shouldReturnOptionalWithDeviceNameIntact() {
        // Given
        String expectedName = "My Test Device";
        Device deviceWithName = new Device(
                deviceId,
                expectedName,
                "Brand",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(deviceWithName));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result.map(Device::name)).contains(expectedName);
    }

    @Test
    @DisplayName("Should return Optional with device brand intact")
    void shouldReturnOptionalWithDeviceBrandIntact() {
        // Given
        String expectedBrand = "Samsung";
        Device deviceWithBrand = new Device(
                deviceId,
                "Device",
                expectedBrand,
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(deviceWithBrand));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result.map(Device::brand)).contains(expectedBrand);
    }

    @Test
    @DisplayName("Should handle empty Optional properly with isPresent")
    void shouldHandleEmptyOptionalProperlyWithIsPresent() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(nonExistentId);

        // Then
        assertThat(result.isPresent()).isFalse();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should handle Optional operations correctly")
    void shouldHandleOptionalOperationsCorrectly() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        Optional<Device> result = getDeviceByIdUseCase.getById(deviceId);

        // Then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isNotNull();
    }
}
