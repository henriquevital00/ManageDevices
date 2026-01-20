package org.example.infra.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.example.domain.enums.DeviceStateEnum;

import java.util.Optional;

@Schema(description = "Request payload for partial updates of an existing device. Only provided fields are updated.")
public record PartialUpdateDeviceRequest(
        @Schema(description = "Updated device name (optional). Cannot be updated if device is IN_USE", example = "Temperature Sensor A1 - Updated")
        Optional<String> name,

        @Schema(description = "Updated device brand/manufacturer (optional, will be normalized to uppercase). Cannot be updated if device is IN_USE", example = "Samsung")
        Optional<String> brand,

        @Schema(description = "Updated device state (optional)", example = "IN_USE", allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"})
        Optional<DeviceStateEnum> state,

        @Schema(description = "Current version of the device (required for optimistic locking). Must match the server version or update will fail with 409 Conflict", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Version is mandatory for optimistic locking")
        Long version
) {
    public PartialUpdateDeviceRequest {
        if (brand.isPresent() && !brand.get().isEmpty()) {
            brand = brand.map(b -> b.toUpperCase());
        }
    }
}
