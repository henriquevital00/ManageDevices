package org.example.app.ports.out;

import org.example.domain.Device;
import org.example.domain.enums.OperationTypeEnum;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;

import java.util.Optional;
import java.util.UUID;

public interface DeviceHistoryRepositoryPort {
    void save(Device device, OperationTypeEnum operationTypeEnum);
}
