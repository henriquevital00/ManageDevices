package org.example.app.usecase;

import org.example.domain.Device;
import org.example.infra.rest.dto.CreateDeviceRequest;

public interface CreateDeviceUseCase {
    Device create(CreateDeviceRequest request);
}
