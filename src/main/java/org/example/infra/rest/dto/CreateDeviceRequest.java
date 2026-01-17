package org.example.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.domain.enums.DeviceStateEnum;

public record CreateDeviceRequest (
        @NotBlank(message = "Name is mandatory") String name,
        @NotBlank(message = "Brand is mandatory") String brand,
        @NotNull(message = "State is mandatory")
        DeviceStateEnum state
){
}
