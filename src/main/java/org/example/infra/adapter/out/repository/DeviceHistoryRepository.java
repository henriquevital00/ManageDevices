package org.example.infra.adapter.out.repository;

import org.example.infra.adapter.persistence.DeviceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DeviceHistoryRepository extends JpaRepository<DeviceHistoryEntity, UUID>, JpaSpecificationExecutor<DeviceHistoryEntity> {
}
