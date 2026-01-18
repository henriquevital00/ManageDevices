package org.example.app.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.app.usecase.DeleteDeviceResult;
import org.example.app.usecase.DeleteDeviceUseCase;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteDeviceImpl implements DeleteDeviceUseCase {

    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public DeleteDeviceResult delete(UUID id) {
        Optional<Device> device = deviceRepositoryPort.findById(id);
        if (device.isEmpty()) {
            return DeleteDeviceResult.NOT_FOUND;
        }

        if (device.get().state() == DeviceStateEnum.IN_USE) {
            return DeleteDeviceResult.IN_USE;
        }

        deviceRepositoryPort.deleteById(id);
        return DeleteDeviceResult.DELETED;
    }
}
