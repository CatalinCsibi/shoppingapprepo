package com.demo.shoppingapp.dto.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        String userId,
        String status,
        List<CartItemResponse> items,
        BigDecimal total
) {}