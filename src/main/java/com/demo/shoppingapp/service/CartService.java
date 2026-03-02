package com.demo.shoppingapp.service;

import com.demo.shoppingapp.dto.request.AddToCartRequest;
import com.demo.shoppingapp.dto.request.UpdateCartItemRequest;
import com.demo.shoppingapp.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getOrCreateCart(String userId);

    CartResponse addToCart(String userId, AddToCartRequest request);

    CartResponse updateCartItem(String userId, UUID itemId, UpdateCartItemRequest request);

    CartResponse removeFromCart(String userId, UUID itemId);

    void clearCart(String userId);
}
