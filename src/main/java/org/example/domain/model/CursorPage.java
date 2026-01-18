package org.example.domain.model;

import java.util.List;
import java.util.UUID;

public record CursorPage<T>(
        List<T> content,
        UUID nextCursor,
        int size,
        boolean hasNext
) {
    public static <T> CursorPage<T> of(List<T> content, UUID nextCursor, int size, boolean hasNext) {
        return new CursorPage<>(content, nextCursor, size, hasNext);
    }
}
