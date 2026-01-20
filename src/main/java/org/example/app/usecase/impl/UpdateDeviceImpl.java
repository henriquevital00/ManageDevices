package org.example.app.usecase.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.app.ports.out.DeviceHistoryRepositoryPort;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.app.usecase.UpdateDeviceUseCase;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.enums.OperationTypeEnum;
import org.example.domain.exception.DeviceInUseException;
import org.example.domain.exception.DeviceNotFoundException;
import org.example.infra.rest.dto.PartialUpdateDeviceRequest;
import org.example.infra.rest.dto.UpdateDeviceRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class
UpdateDeviceImpl implements UpdateDeviceUseCase {

    private final DeviceRepositoryPort deviceRepositoryPort;
    private final DeviceHistoryRepositoryPort deviceHistoryRepositoryPort;

    @Transactional
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
            if (!device.brand().equals(request.brand())) {
                throw new DeviceInUseException("brand", "update");
            }
        }

        Device updatedDevice = new Device(
                device.id(),
                request.name(),
                request.brand(),
                request.state(),
                device.creationTime(),
                request.version()
        );

        deviceHistoryRepositoryPort.save(updatedDevice, OperationTypeEnum.UPDATE);
        return deviceRepositoryPort.save(updatedDevice);
    }

    @Transactional
    @Override
    public Device partialUpdate(UUID id, PartialUpdateDeviceRequest request) {
        Optional<Device> existingDevice = deviceRepositoryPort.findById(id);

        if (existingDevice.isEmpty()) {
            throw new DeviceNotFoundException(id);
        }

        Device device = existingDevice.get();

        if (device.state() == DeviceStateEnum.IN_USE) {
            if (request.name().isPresent() && !device.name().equals(request.name().get())) {
                throw new DeviceInUseException("name", "update");
            }
            if (request.brand().isPresent() && !device.brand().equals(request.brand().get())) {
                throw new DeviceInUseException("brand", "update");
            }
        }

        Device updatedDevice = new Device(
                device.id(),
                request.name().orElse(device.name()),
                request.brand().orElse(device.brand()),
                request.state().orElse(device.state()),
                device.creationTime(),
                request.version()
        );

        deviceHistoryRepositoryPort.save(updatedDevice, OperationTypeEnum.UPDATE);
        return deviceRepositoryPort.save(updatedDevice);
    }
}
