package org.example.app.ports.out;

import org.example.domain.Device;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepositoryPort {
    Device save(Device device);
    Optional<Device> findById(UUID id);
    void deleteById(UUID id);
}
