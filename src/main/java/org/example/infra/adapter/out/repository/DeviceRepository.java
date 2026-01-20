package org.example.infra.adapter.out.repository;

import org.example.domain.enums.DeviceStateEnum;
import org.example.infra.adapter.persistence.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<DeviceEntity, UUID>, JpaSpecificationExecutor<DeviceEntity> {
}
