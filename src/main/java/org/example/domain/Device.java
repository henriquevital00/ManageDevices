package org.example.domain;

import org.example.domain.enums.DeviceStateEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record Device (
        UUID id,
        String name,
        String brand,
        DeviceStateEnum state,
        LocalDateTime creationTime,
        Long version
){
}
