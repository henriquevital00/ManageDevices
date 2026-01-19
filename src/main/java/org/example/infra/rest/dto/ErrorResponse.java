package org.example.infra.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response returned when an API operation fails")
public class ErrorResponse {
    @Schema(description = "Timestamp when the error occurred", example = "2024-01-19T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Validation Failed")
    private String error;

    @Schema(description = "Detailed error message", example = "Invalid request body")
    private String message;

    @Schema(description = "Field-specific validation errors (only present for validation failures)", example = "{\"name\": \"Name is mandatory\", \"brand\": \"Brand is mandatory\"}")
    private Map<String, String> fieldErrors;
}
