package com.demo.shoppingapp.controller;

import com.demo.shoppingapp.dto.request.AddToCartRequest;
import com.demo.shoppingapp.dto.request.UpdateCartItemRequest;
import com.demo.shoppingapp.dto.response.ApiResponse;
import com.demo.shoppingapp.dto.response.CartResponse;
import com.demo.shoppingapp.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Cart management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        CartResponse cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddToCartRequest request) {

        String userId = jwt.getSubject();
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        String userId = jwt.getSubject();
        CartResponse cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId) {

        String userId = jwt.getSubject();
        CartResponse cart = cartService.removeFromCart(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
