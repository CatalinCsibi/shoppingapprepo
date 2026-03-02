package com.demo.shoppingapp.dto.request;

import jakarta.validation.constraints.*;

public record UpdateCartItemRequest(
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        @Max(value = 100, message = "Cannot have more than 100 items")
        Integer quantity
) {}
