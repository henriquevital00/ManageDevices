package org.example.app.ports.out;

import org.example.domain.Device;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepositoryPort {
    Device save(Device device);
    Optional<Device> findById(UUID id);
    void deleteById(UUID id);
    CursorPage<Device> findAllByCursor(DeviceFilter filter, UUID cursor, int size);
}
