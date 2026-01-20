package org.example.app.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.app.usecase.ListDevicesUseCase;
import org.example.domain.Device;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListDevicesImpl implements ListDevicesUseCase {

    private final DeviceRepositoryPort deviceRepositoryPort;

    @Override
    public CursorPage<Device> list(DeviceFilter filter, UUID cursor, int size) {
        validateCursorParams(size);
        return deviceRepositoryPort.findAllByCursor(filter, cursor, size);
    }

    private void validateCursorParams(int size) {
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }
}
