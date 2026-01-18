package org.example.domain.filter;

import org.example.domain.enums.DeviceStateEnum;

import java.util.Optional;

public record DeviceFilter(
        String brand,
        DeviceStateEnum state
) {
    public static DeviceFilter empty() {
        return new DeviceFilter(null, null);
    }
}
