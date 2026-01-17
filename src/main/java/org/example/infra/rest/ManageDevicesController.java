package org.example.infra.rest;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.app.usecase.CreateDeviceUseCase;
import org.example.app.usecase.GetDeviceByIdUseCase;
import org.example.domain.Device;
import org.example.infra.rest.dto.CreateDeviceRequest;
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

    @PostMapping
    public ResponseEntity<Device> createDevice(@Valid @RequestBody CreateDeviceRequest createDeviceRequest){
        Device response = createDeviceUseCase.create(createDeviceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable UUID id){
        Optional<Device> response = getDeviceByIdUseCase.getById(id);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
