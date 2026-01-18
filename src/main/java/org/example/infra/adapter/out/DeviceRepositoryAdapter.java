package org.example.infra.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.app.ports.out.DeviceRepositoryPort;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;
import org.example.infra.adapter.out.repository.DeviceRepository;
import org.example.infra.adapter.persistence.DeviceEntity;
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

    @Override
    public void deleteById(UUID id) {
        deviceRepository.deleteById(id);
    }

    @Override
    public CursorPage<Device> findAllByCursor(DeviceFilter filter, UUID cursor, int size) {
        Specification<DeviceEntity> spec = buildSpecification(filter);

        // Add cursor condition if provided
        if (cursor != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("id").as(String.class), cursor.toString()));
        }

        // Fetch one extra to determine if there's a next page
        Pageable pageable = PageRequest.of(0, size + 1, Sort.by("id").descending());
        List<DeviceEntity> entities = deviceRepository.findAll(spec, pageable).getContent();

        boolean hasNext = entities.size() > size;
        List<Device> restaurants = entities.stream()
                .limit(size)
                .map(this::toDomain)
                .collect(Collectors.toList());

        UUID nextCursor = hasNext && !restaurants.isEmpty()
                ? restaurants.get(restaurants.size() - 1).id()
                : null;

        return CursorPage.of(restaurants, nextCursor, size, hasNext);
    }

    private Specification<DeviceEntity> buildSpecification(DeviceFilter filter) {
        Specification<DeviceEntity> spec = Specification.where(null);

        if(filter.brand() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("brand"), "%" + filter.brand() + "%"));
        }

        if(filter.state() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("state"), "%" + filter.state() + "%"));
        }

        return spec;
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
