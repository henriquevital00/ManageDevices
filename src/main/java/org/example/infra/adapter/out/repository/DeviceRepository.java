package org.example.infra.adapter.out.repository;

import org.example.infra.adapter.persistence.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<DeviceEntity, UUID> {
}
