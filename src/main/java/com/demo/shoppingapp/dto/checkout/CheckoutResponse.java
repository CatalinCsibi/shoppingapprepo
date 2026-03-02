package com.demo.shoppingapp.dto.checkout;

import java.math.BigDecimal;

public record CheckoutResponse(
        Long cartId,
        String userId,
        Integer itemsCount,
        BigDecimal total,
        String status
) {}