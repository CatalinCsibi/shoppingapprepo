package com.demo.shoppingapp.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        String name,

        @Size(max = 2000, message = "Description too long")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Invalid price format")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stockQuantity,

        String category,

        @Pattern(regexp = "^(https?://.*)?$", message = "Invalid URL format")
        String imageUrl
) {}
