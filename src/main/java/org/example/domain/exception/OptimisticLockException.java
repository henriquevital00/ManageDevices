package org.example.domain.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OptimisticLockException extends RuntimeException {
    private final UUID deviceId;

    public OptimisticLockException(UUID deviceId) {
        super(String.format("Device with id %s was modified by another transaction. Please retry the operation.", deviceId));
        this.deviceId = deviceId;
    }
}
