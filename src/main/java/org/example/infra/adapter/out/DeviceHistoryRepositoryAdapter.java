package org.example.infra.adapter.out;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.example.app.ports.out.DeviceHistoryRepositoryPort;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.domain.Device;
import org.example.domain.enums.OperationTypeEnum;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;
import org.example.infra.adapter.out.repository.DeviceHistoryRepository;
import org.example.infra.adapter.out.repository.DeviceRepository;
import org.example.infra.adapter.persistence.DeviceEntity;
import org.example.infra.adapter.persistence.DeviceHistoryEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DeviceHistoryRepositoryAdapter implements DeviceHistoryRepositoryPort {

    private final DeviceHistoryRepository deviceHistoryRepository;

    @Override
    public void save(Device device, OperationTypeEnum operationType) {
        DeviceHistoryEntity entity = toEntity(device, operationType);
        deviceHistoryRepository.save(entity);
    }

    private DeviceHistoryEntity toEntity(Device device, OperationTypeEnum operationType){
        DeviceHistoryEntity deviceHistoryEntity = new DeviceHistoryEntity();
        deviceHistoryEntity.setDeviceId(device.id());
        deviceHistoryEntity.setName(device.name());
        deviceHistoryEntity.setBrand(device.brand());
        deviceHistoryEntity.setState(device.state());
        deviceHistoryEntity.setOperationType(operationType);
        return deviceHistoryEntity;
    }
}
