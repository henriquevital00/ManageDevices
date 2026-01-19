package org.example.app.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.app.usecase.UpdateDeviceUseCase;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.exception.DeviceInUseException;
import org.example.domain.exception.DeviceNotFoundException;
import org.example.infra.rest.dto.UpdateDeviceRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class
UpdateDeviceImpl implements UpdateDeviceUseCase {

    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public Device update(UUID id, UpdateDeviceRequest request) {
        Optional<Device> existingDevice = deviceRepositoryPort.findById(id);

        if (existingDevice.isEmpty()) {
            throw new DeviceNotFoundException(id);
        }

        Device device = existingDevice.get();

        if (device.state() == DeviceStateEnum.IN_USE) {
            if (!device.name().equals(request.name())) {
                throw new DeviceInUseException("name", "update");
            }
            if (!device.brand().equals(normalizeBrand(request.brand()))) {
                throw new DeviceInUseException("brand", "update");
            }
        }

        Device updatedDevice = new Device(
                device.id(),
                request.name(),
                normalizeBrand(request.brand()),
                request.state(),
                device.creationTime()
        );

        return deviceRepositoryPort.save(updatedDevice);
    }

    private String normalizeBrand(String brand) {
        if (brand == null || brand.isEmpty()) {
            return brand;
        }
        return brand.toUpperCase();
    }
}
