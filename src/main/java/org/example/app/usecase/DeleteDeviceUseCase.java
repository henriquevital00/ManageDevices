package org.example.app.usecase;

import java.util.UUID;

public interface DeleteDeviceUseCase {
    DeleteDeviceResult delete(UUID id);
}
