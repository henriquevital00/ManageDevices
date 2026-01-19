package org.example.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.domain.enums.DeviceStateEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Device entity representing a managed device in the system")
public record Device (
        @Schema(description = "Unique device identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Device name")
        String name,

        @Schema(description = "Device brand/manufacturer (stored in uppercase)", example = "SAMSUNG")
        String brand,

        @Schema(description = "Current device state", example = "AVAILABLE", allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"})
        DeviceStateEnum state,

        @Schema(description = "Timestamp when the device was created", example = "2025-01-19T10:30:00")
        LocalDateTime creationTime,

        @Schema(description = "Version number for optimistic locking (increments with each update)", example = "0")
        Long version
){
}
