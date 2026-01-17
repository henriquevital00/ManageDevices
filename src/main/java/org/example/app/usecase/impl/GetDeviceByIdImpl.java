package org.example.app.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.app.usecase.GetDeviceByIdUseCase;
import org.example.domain.Device;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDeviceByIdImpl implements GetDeviceByIdUseCase {
    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public Optional<Device> getById(UUID id) {
        return deviceRepositoryPort.findById(id);
    }
}
