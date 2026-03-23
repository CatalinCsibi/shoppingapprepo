package com.demo.shoppingapp.service.impl;

import com.demo.shoppingapp.dto.request.AddToCartRequest;
import com.demo.shoppingapp.dto.request.UpdateCartItemRequest;
import com.demo.shoppingapp.dto.response.CartResponse;
import com.demo.shoppingapp.exception.BusinessException;
import com.demo.shoppingapp.exception.ResourceNotFoundException;
import com.demo.shoppingapp.mapper.CartMapper;
import com.demo.shoppingapp.model.*;
import com.demo.shoppingapp.repository.CartItemRepository;
import com.demo.shoppingapp.repository.ProductRepository;
import com.demo.shoppingapp.repository.ShoppingCartRepository;
import com.demo.shoppingapp.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    public CartResponse getOrCreateCart(String userId) {
        ShoppingCart cart = getOrCreateActiveCart(userId);
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse addToCart(String userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.productId(), userId);

        Product product = productRepository.findByIdAndActiveTrue(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.productId()));

        if (product.getStockQuantity() < request.quantity()) {
            throw new BusinessException(
                    String.format("Insufficient stock. Available: %d", product.getStockQuantity())
            );
        }

        ShoppingCart cart = getOrCreateActiveCart(userId);

        // Check if product already in cart
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.quantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new BusinessException("Cannot add more items than available in stock");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .priceAtAddition(product.getPrice())
                    .build();
            cart.addItem(newItem);
        }

        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse updateCartItem(String userId, UUID itemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", itemId, userId);

        ShoppingCart cart = getActiveCartOrThrow(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (request.quantity() == 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            if (request.quantity() > item.getProduct().getStockQuantity()) {
                throw new BusinessException("Requested quantity exceeds available stock");
            }
            item.setQuantity(request.quantity());
        }

        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse removeFromCart(String userId, UUID itemId) {
        log.info("Removing item {} from cart for user {}", itemId, userId);

        ShoppingCart cart = getActiveCartOrThrow(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        cart.removeItem(item);
        cartItemRepository.delete(item);

        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public void clearCart(String userId) {
        log.info("Clearing cart for user {}", userId);

        ShoppingCart cart = getActiveCartOrThrow(userId);
        cart.getItems().clear();
        cart.setStatus(CartStatus.ABANDONED);
        cartRepository.save(cart);
    }

    // Helper methods
    private ShoppingCart getOrCreateActiveCart(String userId) {
        return cartRepository
                .findByUserIdAndStatusWithItems(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(userId));
    }

    private ShoppingCart getActiveCartOrThrow(String userId) {
        return cartRepository
                .findByUserIdAndStatusWithItems(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
    }

    private ShoppingCart createNewCart(String userId) {
        ShoppingCart cart = ShoppingCart.builder()
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .build();
        return cartRepository.save(cart);
    }
}
