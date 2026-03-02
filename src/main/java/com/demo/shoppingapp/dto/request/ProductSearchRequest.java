package com.demo.shoppingapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProductSearchRequest(
        @Size(max = 100, message = "Search query too long")
        String query,

        String category,

        @Min(value = 0, message = "Min price cannot be negative")
        Double minPrice,

        @Min(value = 0, message = "Max price cannot be negative")
        Double maxPrice,

        Boolean inStock,

        @Min(value = 0, message = "Page cannot be negative")
        Integer page,

        @Min(value = 1, message = "Size must be at least 1")
        Integer size,

        String sortBy,

        String sortDirection
) {
    public ProductSearchRequest {
        // Defaults
        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sortBy == null) sortBy = "createdAt";
        if (sortDirection == null) sortDirection = "DESC";
    }
}
