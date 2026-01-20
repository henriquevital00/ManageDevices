package org.example.infra.adapter.out;

import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.enums.OperationTypeEnum;
import org.example.infra.adapter.out.repository.DeviceHistoryRepository;
import org.example.infra.adapter.persistence.DeviceHistoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceHistoryRepositoryAdapter Unit Tests")
class DeviceHistoryRepositoryAdapterTest {

    @Mock
    private DeviceHistoryRepository deviceHistoryRepository;

    @InjectMocks
    private DeviceHistoryRepositoryAdapter deviceHistoryRepositoryAdapter;

    @Captor
    private ArgumentCaptor<DeviceHistoryEntity> entityCaptor;

    private UUID deviceId;
    private Device device;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID();
        now = LocalDateTime.now();
        device = new Device(
                deviceId,
                "Test Device",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                now,
                0L
        );
    }

    // ==================== SAVE TESTS ====================

    @Test
    @DisplayName("Should save device history with CREATION operation type")
    void shouldSaveDeviceHistoryWithCreationOperationType() {
        // Given
        OperationTypeEnum operationType = OperationTypeEnum.CREATION;

        // When
        deviceHistoryRepositoryAdapter.save(device, operationType);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getDeviceId()).isEqualTo(deviceId);
        assertThat(savedEntity.getName()).isEqualTo("Test Device");
        assertThat(savedEntity.getBrand()).isEqualTo("SAMSUNG");
        assertThat(savedEntity.getState()).isEqualTo(DeviceStateEnum.AVAILABLE);
        assertThat(savedEntity.getOperationType()).isEqualTo(OperationTypeEnum.CREATION);
    }

    @Test
    @DisplayName("Should save device history with UPDATE operation type")
    void shouldSaveDeviceHistoryWithUpdateOperationType() {
        // Given
        OperationTypeEnum operationType = OperationTypeEnum.UPDATE;

        // When
        deviceHistoryRepositoryAdapter.save(device, operationType);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getOperationType()).isEqualTo(OperationTypeEnum.UPDATE);
    }

    @Test
    @DisplayName("Should save device history with DELETION operation type")
    void shouldSaveDeviceHistoryWithDeletionOperationType() {
        // Given
        OperationTypeEnum operationType = OperationTypeEnum.DELETION;

        // When
        deviceHistoryRepositoryAdapter.save(device, operationType);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getOperationType()).isEqualTo(OperationTypeEnum.DELETION);
    }

    @ParameterizedTest
    @EnumSource(OperationTypeEnum.class)
    @DisplayName("Should save device history for all operation types")
    void shouldSaveDeviceHistoryForAllOperationTypes(OperationTypeEnum operationType) {
        // When
        deviceHistoryRepositoryAdapter.save(device, operationType);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getOperationType()).isEqualTo(operationType);
        assertThat(savedEntity.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    @DisplayName("Should preserve device ID in history entity")
    void shouldPreserveDeviceIdInHistoryEntity() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    @DisplayName("Should preserve device name in history entity")
    void shouldPreserveDeviceNameInHistoryEntity() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getName()).isEqualTo("Test Device");
    }

    @Test
    @DisplayName("Should preserve device brand in history entity")
    void shouldPreserveDeviceBrandInHistoryEntity() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getBrand()).isEqualTo("SAMSUNG");
    }

    @Test
    @DisplayName("Should preserve device state in history entity")
    void shouldPreserveDeviceStateInHistoryEntity() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getState()).isEqualTo(DeviceStateEnum.AVAILABLE);
    }

    @Test
    @DisplayName("Should call repository save method once")
    void shouldCallRepositorySaveMethodOnce() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository, times(1)).save(any(DeviceHistoryEntity.class));
    }

    @Test
    @DisplayName("Should create new entity with null ID for repository to generate")
    void shouldCreateNewEntityWithNullIdForRepositoryToGenerate() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        // Entity ID should be null as it's generated by database
        assertThat(savedEntity.getId()).isNull();
    }

    @Test
    @DisplayName("Should save history for IN_USE device state")
    void shouldSaveHistoryForInUseDeviceState() {
        // Given
        Device inUseDevice = new Device(
                deviceId,
                "In Use Device",
                "LG",
                DeviceStateEnum.IN_USE,
                now,
                1L
        );

        // When
        deviceHistoryRepositoryAdapter.save(inUseDevice, OperationTypeEnum.UPDATE);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getState()).isEqualTo(DeviceStateEnum.IN_USE);
        assertThat(savedEntity.getName()).isEqualTo("In Use Device");
        assertThat(savedEntity.getBrand()).isEqualTo("LG");
    }

    @Test
    @DisplayName("Should save history for INACTIVE device state")
    void shouldSaveHistoryForInactiveDeviceState() {
        // Given
        Device inactiveDevice = new Device(
                deviceId,
                "Inactive Device",
                "APPLE",
                DeviceStateEnum.INACTIVE,
                now,
                2L
        );

        // When
        deviceHistoryRepositoryAdapter.save(inactiveDevice, OperationTypeEnum.UPDATE);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getState()).isEqualTo(DeviceStateEnum.INACTIVE);
    }

    @ParameterizedTest
    @EnumSource(DeviceStateEnum.class)
    @DisplayName("Should save history for all device states")
    void shouldSaveHistoryForAllDeviceStates(DeviceStateEnum state) {
        // Given
        Device deviceWithState = new Device(
                deviceId,
                "Device",
                "Brand",
                state,
                now,
                0L
        );

        // When
        deviceHistoryRepositoryAdapter.save(deviceWithState, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getState()).isEqualTo(state);
    }

    // ==================== DEVICE PROPERTY TESTS ====================

    @Test
    @DisplayName("Should handle device with special characters in name")
    void shouldHandleDeviceWithSpecialCharactersInName() {
        // Given
        Device deviceWithSpecialChars = new Device(
                deviceId,
                "Device-Test_123@Special",
                "Brand",
                DeviceStateEnum.AVAILABLE,
                now,
                0L
        );

        // When
        deviceHistoryRepositoryAdapter.save(deviceWithSpecialChars, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getName()).isEqualTo("Device-Test_123@Special");
    }

    @Test
    @DisplayName("Should handle device with long brand name")
    void shouldHandleDeviceWithLongBrandName() {
        // Given
        Device deviceWithLongBrand = new Device(
                deviceId,
                "Device",
                "VERY_LONG_BRAND_NAME_FOR_TESTING",
                DeviceStateEnum.AVAILABLE,
                now,
                0L
        );

        // When
        deviceHistoryRepositoryAdapter.save(deviceWithLongBrand, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getBrand()).isEqualTo("VERY_LONG_BRAND_NAME_FOR_TESTING");
    }

    @Test
    @DisplayName("Should handle device with different UUID")
    void shouldHandleDeviceWithDifferentUuid() {
        // Given
        UUID differentId = UUID.randomUUID();
        Device deviceWithDifferentId = new Device(
                differentId,
                "Another Device",
                "SAMSUNG",
                DeviceStateEnum.AVAILABLE,
                now,
                0L
        );

        // When
        deviceHistoryRepositoryAdapter.save(deviceWithDifferentId, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getDeviceId()).isEqualTo(differentId);
    }

    @Test
    @DisplayName("Should save multiple history entries for same device")
    void shouldSaveMultipleHistoryEntriesForSameDevice() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.UPDATE);
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.DELETION);

        // Then
        verify(deviceHistoryRepository, times(3)).save(any(DeviceHistoryEntity.class));
    }

    @Test
    @DisplayName("Should save history with correct operation types in sequence")
    void shouldSaveHistoryWithCorrectOperationTypesInSequence() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getOperationType()).isEqualTo(OperationTypeEnum.CREATION);

        // When
        Device updatedDevice = new Device(deviceId, "Updated", "SAMSUNG", DeviceStateEnum.IN_USE, now, 1L);
        deviceHistoryRepositoryAdapter.save(updatedDevice, OperationTypeEnum.UPDATE);

        // Then
        verify(deviceHistoryRepository, times(2)).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getOperationType()).isEqualTo(OperationTypeEnum.UPDATE);
        assertThat(entityCaptor.getValue().getName()).isEqualTo("Updated");
        assertThat(entityCaptor.getValue().getState()).isEqualTo(DeviceStateEnum.IN_USE);
    }

    @Test
    @DisplayName("Should map all device properties to entity")
    void shouldMapAllDevicePropertiesToEntity() {
        // Given
        Device completeDevice = new Device(
                deviceId,
                "Complete Device",
                "APPLE",
                DeviceStateEnum.IN_USE,
                LocalDateTime.of(2025, 1, 1, 12, 0),
                5L
        );

        // When
        deviceHistoryRepositoryAdapter.save(completeDevice, OperationTypeEnum.UPDATE);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getDeviceId()).isEqualTo(deviceId);
        assertThat(savedEntity.getName()).isEqualTo("Complete Device");
        assertThat(savedEntity.getBrand()).isEqualTo("APPLE");
        assertThat(savedEntity.getState()).isEqualTo(DeviceStateEnum.IN_USE);
        assertThat(savedEntity.getOperationType()).isEqualTo(OperationTypeEnum.UPDATE);
    }

    @Test
    @DisplayName("Should not include device version or creation time in history entity")
    void shouldNotIncludeDeviceVersionOrCreationTimeInHistoryEntity() {

        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(entityCaptor.capture());
        DeviceHistoryEntity savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getDeviceId()).isNotNull();
        assertThat(savedEntity.getName()).isNotNull();
        assertThat(savedEntity.getBrand()).isNotNull();
        assertThat(savedEntity.getState()).isNotNull();
        assertThat(savedEntity.getOperationType()).isNotNull();
        assertThat(savedEntity.getCreationTime()).isNull();
    }

    @Test
    @DisplayName("Should verify repository interaction")
    void shouldVerifyRepositoryInteraction() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(any(DeviceHistoryEntity.class));
        verifyNoMoreInteractions(deviceHistoryRepository);
    }

    @Test
    @DisplayName("Should save entity with correct type")
    void shouldSaveEntityWithCorrectType() {
        // When
        deviceHistoryRepositoryAdapter.save(device, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository).save(argThat(entity ->
            entity instanceof DeviceHistoryEntity
        ));
    }

    @Test
    @DisplayName("Should handle consecutive saves independently")
    void shouldHandleConsecutiveSavesIndependently() {
        // Given
        Device device1 = new Device(UUID.randomUUID(), "Device 1", "SAMSUNG", DeviceStateEnum.AVAILABLE, now, 0L);
        Device device2 = new Device(UUID.randomUUID(), "Device 2", "LG", DeviceStateEnum.IN_USE, now, 0L);

        // When
        deviceHistoryRepositoryAdapter.save(device1, OperationTypeEnum.CREATION);
        deviceHistoryRepositoryAdapter.save(device2, OperationTypeEnum.CREATION);

        // Then
        verify(deviceHistoryRepository, times(2)).save(entityCaptor.capture());

        List<DeviceHistoryEntity> savedEntities = entityCaptor.getAllValues();
        assertThat(savedEntities).hasSize(2);
        assertThat(savedEntities.get(0).getName()).isEqualTo("Device 1");
        assertThat(savedEntities.get(0).getBrand()).isEqualTo("SAMSUNG");
        assertThat(savedEntities.get(1).getName()).isEqualTo("Device 2");
        assertThat(savedEntities.get(1).getBrand()).isEqualTo("LG");
    }
}
