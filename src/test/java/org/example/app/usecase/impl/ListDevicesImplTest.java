package org.example.app.usecase.impl;

import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListDevicesImpl Unit Tests")
class ListDevicesImplTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @InjectMocks
    private ListDevicesImpl listDevicesUseCase;

    private DeviceFilter filter;
    private List<Device> devices;

    @BeforeEach
    void setUp() {
        filter = new DeviceFilter("SAMSUNG", DeviceStateEnum.AVAILABLE);

        devices = Arrays.asList(
                new Device(UUID.randomUUID(), "Device 1", "SAMSUNG", DeviceStateEnum.AVAILABLE, LocalDateTime.now(), 0L),
                new Device(UUID.randomUUID(), "Device 2", "SAMSUNG", DeviceStateEnum.AVAILABLE, LocalDateTime.now(), 0L)
        );
    }

    @Test
    @DisplayName("Should list devices with filter and pagination")
    void shouldListDevicesWithFilterAndPagination() {
        // Given
        UUID cursor = UUID.randomUUID();
        int size = 10;
        UUID nextCursor = UUID.randomUUID();
        CursorPage<Device> expectedPage = new CursorPage<>(devices, nextCursor, size, true);

        when(deviceRepositoryPort.findAllByCursor(filter, cursor, size)).thenReturn(expectedPage);

        // When
        CursorPage<Device> result = listDevicesUseCase.list(filter, cursor, size);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(nextCursor);
        assertThat(result.hasNext()).isTrue();

        verify(deviceRepositoryPort).findAllByCursor(filter, cursor, size);
    }

    @Test
    @DisplayName("Should list devices without cursor (first page)")
    void shouldListDevicesWithoutCursor() {
        // Given
        int size = 20;
        CursorPage<Device> expectedPage = new CursorPage<>(devices, null, size, false);

        when(deviceRepositoryPort.findAllByCursor(filter, null, size)).thenReturn(expectedPage);

        // When
        CursorPage<Device> result = listDevicesUseCase.list(filter, null, size);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should list devices with empty filter")
    void shouldListDevicesWithEmptyFilter() {
        // Given
        DeviceFilter emptyFilter = DeviceFilter.empty();
        int size = 10;
        CursorPage<Device> expectedPage = new CursorPage<>(devices, null, size, false);

        when(deviceRepositoryPort.findAllByCursor(emptyFilter, null, size)).thenReturn(expectedPage);

        // When
        CursorPage<Device> result = listDevicesUseCase.list(emptyFilter, null, size);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw exception when size is zero")
    void shouldThrowExceptionWhenSizeIsZero() {
        // When & Then
        assertThatThrownBy(() -> listDevicesUseCase.list(filter, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Size must be between 1 and 100");
    }

    @Test
    @DisplayName("Should throw exception when size is negative")
    void shouldThrowExceptionWhenSizeIsNegative() {
        // When & Then
        assertThatThrownBy(() -> listDevicesUseCase.list(filter, null, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Size must be between 1 and 100");
    }

    @Test
    @DisplayName("Should throw exception when size exceeds maximum")
    void shouldThrowExceptionWhenSizeExceedsMaximum() {
        // When & Then
        assertThatThrownBy(() -> listDevicesUseCase.list(filter, null, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Size must be between 1 and 100");
    }

    @Test
    @DisplayName("Should accept size at lower boundary")
    void shouldAcceptSizeAtLowerBoundary() {
        // Given
        CursorPage<Device> expectedPage = new CursorPage<>(devices, null, 1, false);
        when(deviceRepositoryPort.findAllByCursor(any(), any(), eq(1))).thenReturn(expectedPage);

        // When
        CursorPage<Device> result = listDevicesUseCase.list(filter, null, 1);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should accept size at upper boundary")
    void shouldAcceptSizeAtUpperBoundary() {
        // Given
        CursorPage<Device> expectedPage = new CursorPage<>(devices, null, 100, false);
        when(deviceRepositoryPort.findAllByCursor(any(), any(), eq(100))).thenReturn(expectedPage);

        // When
        CursorPage<Device> result = listDevicesUseCase.list(filter, null, 100);

        // Then
        assertThat(result).isNotNull();
    }
}
