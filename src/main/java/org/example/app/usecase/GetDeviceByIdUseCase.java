package org.example.app.usecase;

import org.example.domain.Device;

import java.util.Optional;
import java.util.UUID;

public interface GetDeviceByIdUseCase {
    Optional<Device> getById(UUID id);
}
