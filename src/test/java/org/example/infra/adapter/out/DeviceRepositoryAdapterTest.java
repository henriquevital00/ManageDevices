package org.example.infra.adapter.out;

import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.exception.OptimisticLockException;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;
import org.example.infra.adapter.out.repository.DeviceRepository;
import org.example.infra.adapter.persistence.DeviceEntity;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceRepositoryAdapter Unit Tests")
class DeviceRepositoryAdapterTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceRepositoryAdapter deviceRepositoryAdapter;

    @Captor
    private ArgumentCaptor<DeviceEntity> deviceEntityCaptor;

    @Captor
    private ArgumentCaptor<Specification<DeviceEntity>> specificationCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private UUID deviceId;
    private Device device;
    private DeviceEntity deviceEntity;
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

        deviceEntity = new DeviceEntity();
        deviceEntity.setId(deviceId);
        deviceEntity.setName("Test Device");
        deviceEntity.setBrand("SAMSUNG");
        deviceEntity.setState(DeviceStateEnum.AVAILABLE);
        deviceEntity.setCreationTime(now);
        deviceEntity.setVersion(0L);
    }

    @Test
    @DisplayName("Should save device successfully")
    void shouldSaveDeviceSuccessfully() {
        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(deviceEntity);

        Device savedDevice = deviceRepositoryAdapter.save(device);

        assertThat(savedDevice).isNotNull();
        assertThat(savedDevice.id()).isEqualTo(deviceId);
        assertThat(savedDevice.name()).isEqualTo("Test Device");
        assertThat(savedDevice.brand()).isEqualTo("SAMSUNG");
        assertThat(savedDevice.state()).isEqualTo(DeviceStateEnum.AVAILABLE);

        verify(deviceRepository).save(deviceEntityCaptor.capture());
        DeviceEntity capturedEntity = deviceEntityCaptor.getValue();
        assertThat(capturedEntity.getName()).isEqualTo("Test Device");
    }

    @Test
    @DisplayName("Should map all device properties when saving")
    void shouldMapAllDevicePropertiesWhenSaving() {
        // Given
        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(deviceEntity);

        // When
        deviceRepositoryAdapter.save(device);

        // Then
        verify(deviceRepository).save(deviceEntityCaptor.capture());
        DeviceEntity captured = deviceEntityCaptor.getValue();

        assertThat(captured.getId()).isEqualTo(deviceId);
        assertThat(captured.getName()).isEqualTo("Test Device");
        assertThat(captured.getBrand()).isEqualTo("SAMSUNG");
        assertThat(captured.getState()).isEqualTo(DeviceStateEnum.AVAILABLE);
        assertThat(captured.getCreationTime()).isEqualTo(now);
        assertThat(captured.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should handle new device without ID")
    void shouldHandleNewDeviceWithoutId() {
        // Given
        Device newDevice = new Device(null, "New Device", "LG", DeviceStateEnum.AVAILABLE, null, null);
        DeviceEntity savedEntity = new DeviceEntity();
        UUID generatedId = UUID.randomUUID();
        savedEntity.setId(generatedId);
        savedEntity.setName("New Device");
        savedEntity.setBrand("LG");
        savedEntity.setState(DeviceStateEnum.AVAILABLE);
        savedEntity.setCreationTime(now);
        savedEntity.setVersion(0L);

        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(savedEntity);

        // When
        Device saved = deviceRepositoryAdapter.save(newDevice);

        // Then
        assertThat(saved.id()).isEqualTo(generatedId);

        verify(deviceRepository).save(deviceEntityCaptor.capture());
        DeviceEntity captured = deviceEntityCaptor.getValue();
        assertThat(captured.getId()).isNull();
    }

    @Test
    @DisplayName("Should throw OptimisticLockException on concurrent modification")
    void shouldThrowOptimisticLockExceptionOnConcurrentModification() {
        // Given
        when(deviceRepository.save(any(DeviceEntity.class)))
                .thenThrow(new jakarta.persistence.OptimisticLockException());

        assertThatThrownBy(() -> deviceRepositoryAdapter.save(device))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessageContaining(deviceId.toString());
    }

    @Test
    @DisplayName("Should throw OptimisticLockException on Spring optimistic locking failure")
    void shouldThrowOptimisticLockExceptionOnSpringOptimisticLockingFailure() {
        // Given
        when(deviceRepository.save(any(DeviceEntity.class)))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException("test", new Exception()));

        assertThatThrownBy(() -> deviceRepositoryAdapter.save(device))
                .isInstanceOf(OptimisticLockException.class);
    }

    @ParameterizedTest
    @EnumSource(DeviceStateEnum.class)
    @DisplayName("Should save device with any state")
    void shouldSaveDeviceWithAnyState(DeviceStateEnum state) {
        // Given
        Device deviceWithState = new Device(deviceId, "Device", "Brand", state, now, 0L);
        DeviceEntity entityWithState = new DeviceEntity();
        entityWithState.setId(deviceId);
        entityWithState.setName("Device");
        entityWithState.setBrand("Brand");
        entityWithState.setState(state);
        entityWithState.setCreationTime(now);
        entityWithState.setVersion(0L);

        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(entityWithState);

        // When
        Device saved = deviceRepositoryAdapter.save(deviceWithState);

        // Then
        assertThat(saved.state()).isEqualTo(state);
    }

    @Test
    @DisplayName("Should find device by ID successfully")
    void shouldFindDeviceByIdSuccessfully() {
        // Given
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(deviceEntity));

        // When
        Optional<Device> found = deviceRepositoryAdapter.findById(deviceId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(deviceId);
        assertThat(found.get().name()).isEqualTo("Test Device");
        assertThat(found.get().brand()).isEqualTo("SAMSUNG");

        verify(deviceRepository).findById(deviceId);
    }

    @Test
    @DisplayName("Should return empty Optional when device not found")
    void shouldReturnEmptyOptionalWhenDeviceNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Device> found = deviceRepositoryAdapter.findById(nonExistentId);

        // Then
        assertThat(found).isEmpty();
        verify(deviceRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should map all entity properties to domain when finding")
    void shouldMapAllEntityPropertiesToDomainWhenFinding() {
        // Given
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(deviceEntity));

        // When
        Optional<Device> found = deviceRepositoryAdapter.findById(deviceId);

        // Then
        assertThat(found).isPresent();
        Device foundDevice = found.get();
        assertThat(foundDevice.id()).isEqualTo(deviceId);
        assertThat(foundDevice.name()).isEqualTo("Test Device");
        assertThat(foundDevice.brand()).isEqualTo("SAMSUNG");
        assertThat(foundDevice.state()).isEqualTo(DeviceStateEnum.AVAILABLE);
        assertThat(foundDevice.creationTime()).isEqualTo(now);
        assertThat(foundDevice.version()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should delete device by ID")
    void shouldDeleteDeviceById() {
        // When
        deviceRepositoryAdapter.deleteById(deviceId);

        // Then
        verify(deviceRepository).deleteById(deviceId);
    }

    @Test
    @DisplayName("Should call repository deleteById exactly once")
    void shouldCallRepositoryDeleteByIdExactlyOnce() {
        // When
        deviceRepositoryAdapter.deleteById(deviceId);

        // Then
        verify(deviceRepository, times(1)).deleteById(deviceId);
        verifyNoMoreInteractions(deviceRepository);
    }

    @Test
    @DisplayName("Should find devices with pagination")
    void shouldFindDevicesWithPagination() {
        // Given
        DeviceFilter filter = DeviceFilter.empty();
        List<DeviceEntity> entities = Arrays.asList(deviceEntity);
        PageImpl<DeviceEntity> page = new PageImpl<>(entities);

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        CursorPage<Device> result = deviceRepositoryAdapter.findAllByCursor(filter, null, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.hasNext()).isFalse();

        verify(deviceRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageSize()).isEqualTo(21); // size + 1
    }

    @Test
    @DisplayName("Should find devices with cursor")
    void shouldFindDevicesWithCursor() {
        // Given
        UUID cursor = UUID.randomUUID();
        DeviceFilter filter = DeviceFilter.empty();
        List<DeviceEntity> entities = Arrays.asList(deviceEntity);
        PageImpl<DeviceEntity> page = new PageImpl<>(entities);

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        CursorPage<Device> result = deviceRepositoryAdapter.findAllByCursor(filter, cursor, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);

        verify(deviceRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should indicate hasNext when more results available")
    void shouldIndicateHasNextWhenMoreResultsAvailable() {
        // Given
        DeviceFilter filter = DeviceFilter.empty();
        DeviceEntity entity1 = new DeviceEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setName("Device 1");
        entity1.setBrand("Brand");
        entity1.setState(DeviceStateEnum.AVAILABLE);
        entity1.setCreationTime(now);
        entity1.setVersion(0L);

        DeviceEntity entity2 = new DeviceEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setName("Device 2");
        entity2.setBrand("Brand");
        entity2.setState(DeviceStateEnum.AVAILABLE);
        entity2.setCreationTime(now);
        entity2.setVersion(0L);

        List<DeviceEntity> entities = Arrays.asList(entity1, entity2);
        PageImpl<DeviceEntity> page = new PageImpl<>(entities);

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        CursorPage<Device> result = deviceRepositoryAdapter.findAllByCursor(filter, null, 1);

        // Then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).hasSize(1);
        assertThat(result.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("Should filter by brand")
    void shouldFilterByBrand() {
        // Given
        DeviceFilter filter = new DeviceFilter("SAMSUNG", null, null);
        List<DeviceEntity> entities = List.of(deviceEntity);
        PageImpl<DeviceEntity> page = new PageImpl<>(entities);

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        CursorPage<Device> result = deviceRepositoryAdapter.findAllByCursor(filter, null, 20);

        // Then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).brand()).isEqualTo("SAMSUNG");
    }

    @Test
    @DisplayName("Should filter by state")
    void shouldFilterByState() {
        // Given
        DeviceFilter filter = new DeviceFilter(null, DeviceStateEnum.AVAILABLE, null);
        List<DeviceEntity> entities = List.of(deviceEntity);
        PageImpl<DeviceEntity> page = new PageImpl<>(entities);

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        CursorPage<Device> result = deviceRepositoryAdapter.findAllByCursor(filter, null, 20);

        // Then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).state()).isEqualTo(DeviceStateEnum.AVAILABLE);
    }

    @Test
    @DisplayName("Should filter by brand and state combined")
    void shouldFilterByBrandAndStateCombined() {
        // Given
        DeviceFilter filter = new DeviceFilter("SAMSUNG", DeviceStateEnum.AVAILABLE, null);
        List<DeviceEntity> entities = List.of(deviceEntity);
        PageImpl<DeviceEntity> page = new PageImpl<>(entities);

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        CursorPage<Device> result = deviceRepositoryAdapter.findAllByCursor(filter, null, 20);

        // Then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).brand()).isEqualTo("SAMSUNG");
        assertThat(result.content().get(0).state()).isEqualTo(DeviceStateEnum.AVAILABLE);
    }

    @Test
    @DisplayName("Should return empty page when no devices match filter")
    void shouldReturnEmptyPageWhenNoDevicesMatchFilter() {
        // Given
        DeviceFilter filter = new DeviceFilter("NONEXISTENT", null, null);
        PageImpl<DeviceEntity> emptyPage = new PageImpl<>(Collections.emptyList());

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        CursorPage<Device> result = deviceRepositoryAdapter.findAllByCursor(filter, null, 20);

        // Then
        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    @DisplayName("Should set nextCursor to null when no more pages")
    void shouldSetNextCursorToNullWhenNoMorePages() {
        // Given
        DeviceFilter filter = DeviceFilter.empty();
        List<DeviceEntity> entities = List.of(deviceEntity);
        PageImpl<DeviceEntity> page = new PageImpl<>(entities);

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        CursorPage<Device> result = deviceRepositoryAdapter.findAllByCursor(filter, null, 20);

        // Then
        assertThat(result.nextCursor()).isNull();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should request one extra item for hasNext calculation")
    void shouldRequestOneExtraItemForHasNextCalculation() {
        // Given
        DeviceFilter filter = DeviceFilter.empty();
        int requestedSize = 10;
        PageImpl<DeviceEntity> page = new PageImpl<>(Collections.emptyList());

        when(deviceRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        deviceRepositoryAdapter.findAllByCursor(filter, null, requestedSize);

        // Then
        verify(deviceRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageSize()).isEqualTo(requestedSize + 1);
    }

    @Test
    @DisplayName("Should handle device with null creationTime when converting to entity")
    void shouldHandleDeviceWithNullCreationTimeWhenConvertingToEntity() {
        // Given
        Device deviceWithoutTime = new Device(deviceId, "Device", "Brand", DeviceStateEnum.AVAILABLE, null, 0L);
        DeviceEntity savedEntity = new DeviceEntity();
        savedEntity.setId(deviceId);
        savedEntity.setName("Device");
        savedEntity.setBrand("Brand");
        savedEntity.setState(DeviceStateEnum.AVAILABLE);
        savedEntity.setCreationTime(now);
        savedEntity.setVersion(0L);

        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(savedEntity);

        // When
        Device saved = deviceRepositoryAdapter.save(deviceWithoutTime);

        // Then
        assertThat(saved).isNotNull();

        verify(deviceRepository).save(deviceEntityCaptor.capture());
        DeviceEntity captured = deviceEntityCaptor.getValue();
        assertThat(captured.getCreationTime()).isNull();
    }

    @Test
    @DisplayName("Should handle device with null version when converting to entity")
    void shouldHandleDeviceWithNullVersionWhenConvertingToEntity() {
        // Given
        Device deviceWithoutVersion = new Device(deviceId, "Device", "Brand", DeviceStateEnum.AVAILABLE, now, null);
        DeviceEntity savedEntity = new DeviceEntity();
        savedEntity.setId(deviceId);
        savedEntity.setName("Device");
        savedEntity.setBrand("Brand");
        savedEntity.setState(DeviceStateEnum.AVAILABLE);
        savedEntity.setCreationTime(now);
        savedEntity.setVersion(0L);

        when(deviceRepository.save(any(DeviceEntity.class))).thenReturn(savedEntity);

        // When
        Device saved = deviceRepositoryAdapter.save(deviceWithoutVersion);

        // Then
        assertThat(saved).isNotNull();

        verify(deviceRepository).save(deviceEntityCaptor.capture());
        DeviceEntity captured = deviceEntityCaptor.getValue();
        assertThat(captured.getVersion()).isNull();
    }
}
