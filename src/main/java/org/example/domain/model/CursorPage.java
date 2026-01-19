package org.example.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Paginated response using cursor-based pagination")
public record CursorPage<T>(
        @Schema(description = "List of items in the current page")
        List<T> content,

        @Schema(description = "Cursor to fetch the next page (null if no more pages)", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID nextCursor,

        @Schema(description = "Number of items requested per page", example = "20")
        int size,

        @Schema(description = "Indicates if there are more pages available", example = "true")
        boolean hasNext
) {
    public static <T> CursorPage<T> of(List<T> content, UUID nextCursor, int size, boolean hasNext) {
        return new CursorPage<>(content, nextCursor, size, hasNext);
    }
}
