package com.demo.shoppingapp.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        String status,
        List<CartItemResponse> items,
        Integer totalItems,
        BigDecimal totalPrice
) {}
