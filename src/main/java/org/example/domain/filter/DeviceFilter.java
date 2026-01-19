package org.example.domain.filter;

import org.example.domain.enums.DeviceStateEnum;

import java.util.Optional;

public record DeviceFilter(
        String brand,
        DeviceStateEnum state,
        String searchTerm
) {
    public static DeviceFilter empty() {
        return new DeviceFilter(null, null, null);
    }

    public String normalizedSearchTerm() {
        return Optional.ofNullable(searchTerm)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .orElse(null);
    }
}
