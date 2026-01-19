package org.example.infra.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.example.domain.filter.DeviceFilter;
import org.example.domain.model.CursorPage;
import org.example.infra.rest.dto.CreateDeviceRequest;
import org.example.infra.rest.dto.ErrorResponse;
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
@Tag(name = "Device Management", description = "APIs for managing devices with full CRUD operations, optimistic locking, and history tracking")
public class ManageDevicesController {
    private final CreateDeviceUseCase createDeviceUseCase;
    private final GetDeviceByIdUseCase getDeviceByIdUseCase;
    private final DeleteDeviceUseCase deleteDeviceUseCase;
    private final ListDevicesUseCase listDevicesUseCase;
    private final UpdateDeviceUseCase updateDeviceUseCase;

    @PostMapping
    @Operation(
            summary = "Create a new device",
            description = """
                    Creates a new device in the system with the provided details.
                    - A unique UUID is automatically generated for the device
                    - Creation timestamp is automatically recorded
                    - Initial version is set to 0 for optimistic locking
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Device created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Device.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "123e4567-e89b-12d3-a456-426614174000",
                                      "name": "string",
                                      "brand": "Apple",
                                      "state": "AVAILABLE",
                                      "creationTime": "2024-01-19T10:30:00",
                                      "version": 0
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2024-01-19T10:30:00",
                                      "status": 400,
                                      "error": "Validation Failed",
                                      "message": "Invalid request body",
                                      "fieldErrors": {
                                        "name": "Name is mandatory",
                                        "brand": "Brand is mandatory"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "timestamp": "2026-01-19T07:52:38.938927085",
                                        "status": 500,
                                        "error": "Internal Server Error",
                                        "message": "An unexpected error occurred. Please try again later."
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Device> createDevice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Device creation details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateDeviceRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "String",
                                      "brand": "Apple",
                                      "state": "AVAILABLE"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody CreateDeviceRequest createDeviceRequest){
        Device response = createDeviceUseCase.create(createDeviceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List devices with cursor-based pagination",
            description = """
                    Retrieves a paginated list of devices with optional filtering.
                    - Uses cursor-based pagination for efficient data retrieval
                    - Default page size is 20 items
                    - Returns a cursor for fetching the next page
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Devices retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CursorPage.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "content": [
                                        {
                                          "id": "123e4567-e89b-12d3-a456-426614174000",
                                          "name": "string",
                                          "brand": "Apple",
                                          "state": "AVAILABLE",
                                          "creationTime": "2024-01-19T10:30:00",
                                          "version": 0
                                        }
                                      ],
                                      "nextCursor": "123e4567-e89b-12d3-a456-426614174001",
                                      "size": 20,
                                      "hasNext": true
                                    }
                                    "nextCursor": null,
                                    "size": 20,
                                    "hasNext": false
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<CursorPage<Device>> listDevices(
            @Parameter(description = "Filter by device brand (case-insensitive, will be normalized to uppercase)", example = "Apple")
            @RequestParam(required = false) String brand,

            @Parameter(description = "Filter by device state", example = "AVAILABLE", schema = @Schema(allowableValues = {"AVAILABLE", "IN_USE", "INACTIVE"}))
            @RequestParam(required = false) DeviceStateEnum state,

            @Parameter(description = "Cursor for pagination (UUID of the last device from previous page)")
            @RequestParam(required = false) UUID cursor,

            @Parameter(description = "Number of devices to return per page", example = "20")
            @RequestParam(defaultValue = "20") int size){
        String normalizedBrand = brand != null ? brand.toUpperCase() : null;
        DeviceFilter filter = new DeviceFilter(normalizedBrand, state);
        CursorPage<Device> devices = listDevicesUseCase.list(filter, cursor, size);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get device by ID",
            description = "Retrieves a single device by its unique identifier (UUID)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Device found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Device.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "123e4567-e89b-12d3-a456-426614174000",
                                      "name": "string",
                                      "brand": "Apple",
                                      "state": "AVAILABLE",
                                      "creationTime": "2024-01-19T10:30:00",
                                      "version": 0
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Device> getDeviceById(
            @Parameter(description = "Device UUID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id){
        Optional<Device> response = getDeviceByIdUseCase.getById(id);
        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a device",
            description = """
                    Deletes a device from the system.
                    - Devices in IN_USE state cannot be deleted (returns 409 Conflict)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Device deleted successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Device is IN_USE and cannot be deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2024-01-19T10:30:00",
                                      "status": 409,
                                      "error": "Device In Use",
                                      "message": "Cannot delete device while device is IN_USE"
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Void> deleteDeviceById(
            @Parameter(description = "Device UUID to delete", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id){
        DeleteDeviceResult result = deleteDeviceUseCase.delete(id);
        return switch (result) {
            case DELETED -> ResponseEntity.noContent().build();
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            case IN_USE -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a device",
            description = """
                    Updates an existing device with optimistic locking support.
                    - Brand names are automatically normalized to uppercase
                    - Uses optimistic locking (version field) to prevent concurrent modifications
                    - If version mismatch occurs, returns 409 Conflict with OptimisticLockException
                    - Devices in IN_USE state have restrictions on certain field updates
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Device updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Device.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "123e4567-e89b-12d3-a456-426614174000",
                                      "name": "Temperature Sensor A1 - Updated",
                                      "brand": "SAMSUNG",
                                      "state": "IN_USE",
                                      "creationTime": "2024-01-19T10:30:00",
                                      "version": 1
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2024-01-19T10:30:00",
                                      "status": 404,
                                      "error": "Device Not Found",
                                      "message": "Device not found with id: 123e4567-e89b-12d3-a456-426614174000"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Optimistic locking failure or device in use",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2024-01-19T10:30:00",
                                      "status": 409,
                                      "error": "Concurrent Modification",
                                      "message": "The device has been modified by another user. Please refresh and try again."
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<Device> updateDevice(
            @Parameter(description = "Device UUID to update", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated device details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateDeviceRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Temperature Sensor A1 - Updated",
                                      "brand": "Samsung",
                                      "state": "IN_USE"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody UpdateDeviceRequest updateDeviceRequest) {
        Device updatedDevice = updateDeviceUseCase.update(id, updateDeviceRequest);
        return ResponseEntity.ok(updatedDevice);
    }
}
