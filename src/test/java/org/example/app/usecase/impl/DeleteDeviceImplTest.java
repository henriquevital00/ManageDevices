package org.example.app.usecase.impl;

import org.example.app.ports.out.DeviceHistoryRepositoryPort;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.app.usecase.DeleteDeviceResult;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.enums.OperationTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteDeviceImpl Unit Tests")
class DeleteDeviceImplTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @Mock
    private DeviceHistoryRepositoryPort deviceHistoryRepositoryPort;

    @InjectMocks
    private DeleteDeviceImpl deleteDeviceUseCase;

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
    @DisplayName("Should delete device successfully and return DELETED")
    void shouldDeleteDeviceSuccessfullyAndReturnDeleted() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(deviceId);

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.DELETED);
        verify(deviceRepositoryPort).findById(deviceId);
        verify(deviceHistoryRepositoryPort).save(device, OperationTypeEnum.DELETION);
        verify(deviceRepositoryPort).deleteById(deviceId);
    }

    @Test
    @DisplayName("Should return NOT_FOUND when device does not exist")
    void shouldReturnNotFoundWhenDeviceDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(nonExistentId);

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.NOT_FOUND);
        verify(deviceRepositoryPort).findById(nonExistentId);
        verify(deviceHistoryRepositoryPort, never()).save(any(), any());
        verify(deviceRepositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should return IN_USE when device is IN_USE state")
    void shouldReturnInUseWhenDeviceIsInUseState() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Test Device",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(deviceId);

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.IN_USE);
        verify(deviceRepositoryPort).findById(deviceId);
        verify(deviceHistoryRepositoryPort, never()).save(any(), any());
        verify(deviceRepositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should allow deletion of AVAILABLE device")
    void shouldAllowDeletionOfAvailableDevice() {
        // Given
        Device availableDevice = new Device(
                deviceId,
                "Test Device",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(availableDevice));

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(deviceId);

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.DELETED);
        verify(deviceRepositoryPort).deleteById(deviceId);
    }

    @Test
    @DisplayName("Should allow deletion of INACTIVE device")
    void shouldAllowDeletionOfInactiveDevice() {
        // Given
        Device inactiveDevice = new Device(
                deviceId,
                "Test Device",
                "SAMSUNG",
                DeviceStateEnum.INACTIVE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inactiveDevice));

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(deviceId);

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.DELETED);
        verify(deviceRepositoryPort).deleteById(deviceId);
    }

    @ParameterizedTest
    @EnumSource(value = DeviceStateEnum.class, names = {"AVAILABLE", "INACTIVE"})
    @DisplayName("Should delete device successfully for AVAILABLE and INACTIVE states")
    void shouldDeleteDeviceSuccessfullyForDeletableStates(DeviceStateEnum state) {
        // Given
        Device testDevice = new Device(
                deviceId,
                "Test Device",
                "SAMSUNG",
                state,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(testDevice));

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(deviceId);

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.DELETED);
        verify(deviceRepositoryPort).deleteById(deviceId);
        verify(deviceHistoryRepositoryPort).save(testDevice, OperationTypeEnum.DELETION);
    }

    @Test
    @DisplayName("Should record device history before deletion")
    void shouldRecordDeviceHistoryBeforeDeletion() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        deleteDeviceUseCase.delete(deviceId);

        // Then
        verify(deviceHistoryRepositoryPort).save(device, OperationTypeEnum.DELETION);
    }

    @Test
    @DisplayName("Should use DELETION operation type for history")
    void shouldUseDeletionOperationTypeForHistory() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        deleteDeviceUseCase.delete(deviceId);

        // Then
        verify(deviceHistoryRepositoryPort).save(eq(device), eq(OperationTypeEnum.DELETION));
    }

    @Test
    @DisplayName("Should delete from repository with correct ID")
    void shouldDeleteFromRepositoryWithCorrectId() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        deleteDeviceUseCase.delete(deviceId);

        // Then
        verify(deviceRepositoryPort).deleteById(deviceId);
    }

    @Test
    @DisplayName("Should not delete when device is IN_USE regardless of other properties")
    void shouldNotDeleteWhenDeviceIsInUseRegardlessOfOtherProperties() {
        // Given
        Device inUseDeviceWithVersion = new Device(
                deviceId,
                "Important Device",
                "CRITICAL_BRAND",
                DeviceStateEnum.IN_USE,
                LocalDateTime.now().minusDays(365),
                10L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDeviceWithVersion));

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(deviceId);

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.IN_USE);
        verify(deviceRepositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should return correct result for each deletion outcome")
    void shouldReturnCorrectResultForEachDeletionOutcome() {
        // Test 1: Device found and deleted
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        assertThat(deleteDeviceUseCase.delete(deviceId)).isEqualTo(DeleteDeviceResult.DELETED);

        // Test 2: Device not found
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThat(deleteDeviceUseCase.delete(nonExistentId)).isEqualTo(DeleteDeviceResult.NOT_FOUND);

        // Test 3: Device in use
        Device inUseDevice = new Device(deviceId, "Name", "Brand", DeviceStateEnum.IN_USE, LocalDateTime.now(), 0L);
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));
        assertThat(deleteDeviceUseCase.delete(deviceId)).isEqualTo(DeleteDeviceResult.IN_USE);
    }

    @Test
    @DisplayName("Should handle device with different properties correctly")
    void shouldHandleDeviceWithDifferentPropertiesCorrectly() {
        // Given
        Device deviceWithDifferentProps = new Device(
                UUID.randomUUID(),
                "Another Device",
                "LG",
                DeviceStateEnum.AVAILABLE,
                LocalDateTime.now().minusDays(100),
                5L
        );
        when(deviceRepositoryPort.findById(deviceWithDifferentProps.id())).thenReturn(Optional.of(deviceWithDifferentProps));

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(deviceWithDifferentProps.id());

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.DELETED);
        verify(deviceRepositoryPort).deleteById(deviceWithDifferentProps.id());
    }

    @Test
    @DisplayName("Should verify repository interactions for successful deletion")
    void shouldVerifyRepositoryInteractionsForSuccessfulDeletion() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        DeleteDeviceResult result = deleteDeviceUseCase.delete(deviceId);

        // Then
        assertThat(result).isEqualTo(DeleteDeviceResult.DELETED);

        // Verify order of operations: findById → save history → deleteById
        verify(deviceRepositoryPort).findById(deviceId);
        verify(deviceHistoryRepositoryPort).save(device, OperationTypeEnum.DELETION);
        verify(deviceRepositoryPort).deleteById(deviceId);
    }

    @Test
    @DisplayName("Should not attempt any operations when device not found")
    void shouldNotAttemptAnyOperationsWhenDeviceNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        deleteDeviceUseCase.delete(nonExistentId);

        // Then
        verify(deviceRepositoryPort).findById(nonExistentId);
        verify(deviceHistoryRepositoryPort, never()).save(any(), any());
        verify(deviceRepositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should not attempt any operations when device is IN_USE")
    void shouldNotAttemptAnyOperationsWhenDeviceIsInUse() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "Test Device",
                "SAMSUNG",
                DeviceStateEnum.IN_USE,
                LocalDateTime.now(),
                0L
        );
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(inUseDevice));

        // When
        deleteDeviceUseCase.delete(deviceId);

        // Then
        verify(deviceRepositoryPort).findById(deviceId);
        verify(deviceHistoryRepositoryPort, never()).save(any(), any());
        verify(deviceRepositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should use findById from repository")
    void shouldUseFindByIdFromRepository() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        deleteDeviceUseCase.delete(deviceId);

        // Then
        verify(deviceRepositoryPort).findById(deviceId);
    }

    @Test
    @DisplayName("Should preserve device state during deletion process")
    void shouldPreserveDeviceStateDuringDeletionProcess() {
        // Given
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        // When
        deleteDeviceUseCase.delete(deviceId);

        // Then
        // Verify that the same device object is used for history
        verify(deviceHistoryRepositoryPort).save(device, OperationTypeEnum.DELETION);
        assertThat(device.state()).isEqualTo(DeviceStateEnum.AVAILABLE);
    }
}
