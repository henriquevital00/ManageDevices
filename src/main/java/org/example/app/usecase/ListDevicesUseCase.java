package org.example.app.usecase;

import org.example.domain.Device;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;

import java.util.List;
import java.util.UUID;

public interface ListDevicesUseCase {
    CursorPage<Device> list(DeviceFilter filter, UUID cursor, int size);
}
