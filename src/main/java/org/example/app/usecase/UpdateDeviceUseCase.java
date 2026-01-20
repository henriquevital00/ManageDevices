package org.example.app.usecase;

import org.example.domain.Device;
import org.example.infra.rest.dto.UpdateDeviceRequest;
import org.example.infra.rest.dto.PartialUpdateDeviceRequest;

import java.util.UUID;

public interface UpdateDeviceUseCase {
    Device update(UUID id, UpdateDeviceRequest request);

    Device partialUpdate(UUID id, PartialUpdateDeviceRequest request);
}
