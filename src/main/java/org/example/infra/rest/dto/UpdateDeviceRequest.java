package org.example.infra.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.domain.enums.DeviceStateEnum;

@Schema(description = "Request payload for updating an existing device with optimistic locking support")
public record UpdateDeviceRequest(
        @Schema(description = "Updated device name", example = "Temperature Sensor A1 - Updated", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is mandatory")
        String name,

        @Schema(description = "Updated device brand/manufacturer (will be normalized to uppercase)", example = "Samsung", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Brand is mandatory")
        String brand,

        @Schema(description = "Updated device state", example = "IN_USE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"})
        @NotNull(message = "State is mandatory")
        DeviceStateEnum state,

        @Schema(description = "Current version of the device (required for optimistic locking). Must match the server version or update will fail with 409 Conflict", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Version is mandatory for optimistic locking")
        Long version
) {
    public UpdateDeviceRequest {
        brand = normalizeBrand(brand);
    }

    private static String normalizeBrand(String brand) {
        if (brand == null || brand.isEmpty()) {
            return brand;
        }
        return brand.toUpperCase();
    }
}


