package com.demo.shoppingapp.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record AddToCartRequest(
        @NotNull(message = "Product ID is required")
        UUID productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100, message = "Cannot add more than 100 items")
        Integer quantity
) {}
