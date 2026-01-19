package org.example.infra.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.domain.enums.DeviceStateEnum;

@Schema(description = "Request payload for creating a new device")
public record CreateDeviceRequest (
        @Schema(description = "Device name", example = "Temperature Sensor A1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is mandatory")
        String name,

        @Schema(description = "Device brand/manufacturer (will be normalized to uppercase)", example = "Samsung", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Brand is mandatory")
        String brand,

        @Schema(description = "Initial state of the device", example = "AVAILABLE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"})
        @NotNull(message = "State is mandatory")
        DeviceStateEnum state
){
}
