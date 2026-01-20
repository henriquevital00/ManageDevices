package org.example.domain.exception;

public class DeviceInUseException extends RuntimeException {
    public DeviceInUseException(String message) {
        super(message);
    }

    public DeviceInUseException(String property, String action) {
        super("Cannot " + action + " " + property + " while device is IN_USE");
    }
}
