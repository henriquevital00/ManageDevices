package org.example.infra.rest;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.app.usecase.CreateDeviceUseCase;
import org.example.app.usecase.DeleteDeviceResult;
import org.example.app.usecase.DeleteDeviceUseCase;
import org.example.app.usecase.GetDeviceByIdUseCase;
import org.example.app.usecase.ListDevicesUseCase;
import org.example.app.usecase.UpdateDeviceUseCase;
import org.example.domain.Device;
import org.example.domain.enums.DeviceStateEnum;
import org.example.domain.exception.DeviceInUseException;
import org.example.domain.exception.DeviceNotFoundException;
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;
import org.example.infra.rest.dto.CreateDeviceRequest;
import org.example.infra.rest.dto.UpdateDeviceRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path="/v1/devices", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
@Validated
@AllArgsConstructor
@Log4j2
public class ManageDevicesController {
    private final CreateDeviceUseCase createDeviceUseCase;
    private final GetDeviceByIdUseCase getDeviceByIdUseCase;
    private final DeleteDeviceUseCase deleteDeviceUseCase;
    private final ListDevicesUseCase listDevicesUseCase;
    private final UpdateDeviceUseCase updateDeviceUseCase;

    @PostMapping
    public ResponseEntity<Device> createDevice(@Valid @RequestBody CreateDeviceRequest createDeviceRequest){
        Device response = createDeviceUseCase.create(createDeviceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CursorPage<Device>> listDevices(@RequestParam(required = false) String brand,
                                                    @RequestParam(required = false) DeviceStateEnum state,
                                                    @RequestParam(required = false) UUID cursor,
                                                    @RequestParam(defaultValue = "20") int size){
        String normalizedBrand = brand != null ? brand.toUpperCase() : null;
        DeviceFilter filter = new DeviceFilter(normalizedBrand, state);
        CursorPage<Device> devices = listDevicesUseCase.list(filter, cursor, size);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable UUID id){
        Optional<Device> response = getDeviceByIdUseCase.getById(id);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceById(@PathVariable UUID id){
        DeleteDeviceResult result = deleteDeviceUseCase.delete(id);
        return switch (result) {
            case DELETED -> ResponseEntity.noContent().build();
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            case IN_USE -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateDeviceRequest updateDeviceRequest) {
        try {
            Device updatedDevice = updateDeviceUseCase.update(id, updateDeviceRequest);
            return ResponseEntity.ok(updatedDevice);
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DeviceInUseException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
