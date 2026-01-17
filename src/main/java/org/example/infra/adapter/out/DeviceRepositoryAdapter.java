package org.example.infra.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.domain.Device;
import org.example.infra.adapter.out.repository.DeviceRepository;
import org.example.infra.adapter.persistence.DeviceEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DeviceRepositoryAdapter implements DeviceRepositoryPort {

    private final DeviceRepository deviceRepository;

    @Override
    public Device save(Device device) {
        DeviceEntity entity = toEntity(device);
        DeviceEntity saved = deviceRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Device> findById(UUID id) {
        return deviceRepository.findById(id).map(this::toDomain);
    }

    private Device toDomain(DeviceEntity entity) {
        return new Device(
                entity.getId(),
                entity.getName(),
                entity.getBrand(),
                entity.getState(),
                entity.getCreationTime()
        );
    }

    private DeviceEntity toEntity(Device device){
        DeviceEntity deviceEntity = new DeviceEntity();
        if (device.id() != null) {
            deviceEntity.setId(device.id());
        }
        deviceEntity.setName(device.name());
        deviceEntity.setBrand(device.brand());
        deviceEntity.setState(device.state());
        if (device.creationTime() != null) {
            deviceEntity.setCreationTime(device.creationTime());
        }
        return deviceEntity;
    }
}
