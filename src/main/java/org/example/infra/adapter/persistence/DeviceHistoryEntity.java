package org.example.infra.adapter.persistence;

import jakarta.persistence.*;
import lombok.Data;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.enums.OperationTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "device_history", schema = "manage_devices")
public class DeviceHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID deviceId;
    @Column(nullable = false, updatable = false)
    private String name;
    @Column(nullable = false, updatable = false)
    private String brand;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DeviceStateEnum state;
    @Column(nullable = false, updatable = false)
    private LocalDateTime creationTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OperationTypeEnum operationType;

    @PrePersist
    protected void onCreate() {
        creationTime = LocalDateTime.now();
    }
}
