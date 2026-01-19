package org.example.infra.adapter.persistence;

import jakarta.persistence.*;
import lombok.Data;
import org.example.domain.enums.DeviceStateEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "device", schema = "manage_devices")
public class DeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStateEnum state;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creationTime;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {
        creationTime = LocalDateTime.now();
    }
}
