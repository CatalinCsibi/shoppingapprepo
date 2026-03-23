package com.demo.shoppingapp.support;

import com.demo.shoppingapp.dto.request.AddToCartRequest;
import com.demo.shoppingapp.dto.request.CreateProductRequest;
import com.demo.shoppingapp.dto.request.ProductSearchRequest;
import com.demo.shoppingapp.dto.request.UpdateCartItemRequest;
import com.demo.shoppingapp.dto.response.CartItemResponse;
import com.demo.shoppingapp.dto.response.CartResponse;
import com.demo.shoppingapp.dto.response.ProductResponse;
import com.demo.shoppingapp.model.CartItem;
import com.demo.shoppingapp.model.CartStatus;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.model.ShoppingCart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Product product() {
        return product(UUID.randomUUID(), "iPhone 15", 20);
    }

    public static Product product(UUID id, String name, int stockQuantity) {
        return Product.builder()
                .id(id)
                .name(name)
                .description(name + " description")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(stockQuantity)
                .category("phones")
                .imageUrl("https://example.com/" + name.replace(" ", "-") + ".png")
                .active(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 2, 12, 0))
                .version(0L)
                .build();
    }

    public static Product inactiveProduct(UUID id, String name) {
        Product product = product(id, name, 0);
        product.setActive(false);
        return product;
    }

    public static CreateProductRequest createProductRequest() {
        return new CreateProductRequest(
                "MacBook Pro",
                "High-end laptop",
                new BigDecimal("2499.99"),
                5,
                "laptops",
                "https://example.com/macbook.png"
        );
    }

    public static ProductSearchRequest productSearchRequest() {
        return new ProductSearchRequest(
                "iphone",
                "phones",
                1000.0,
                1500.0,
                true,
                0,
                20,
                "name",
                "ASC"
        );
    }

    public static ProductResponse productResponse(UUID id) {
        return new ProductResponse(
                id,
                "iPhone 15",
                "Latest Apple smartphone",
                new BigDecimal("1299.99"),
                20,
                "phones",
                "https://example.com/iphone.png",
                true,
                LocalDateTime.of(2025, 1, 1, 12, 0)
        );
    }

    public static ShoppingCart shoppingCart(String userId) {
        return ShoppingCart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .build();
    }

    public static CartItem cartItem(ShoppingCart cart, Product product, int quantity) {
        return CartItem.builder()
                .id(UUID.randomUUID())
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .priceAtAddition(product.getPrice())
                .build();
    }

    public static CartResponse cartResponse(UUID cartId) {
        CartItemResponse itemResponse = new CartItemResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "iPhone 15",
                "https://example.com/iphone.png",
                2,
                new BigDecimal("1299.99"),
                new BigDecimal("2599.98")
        );

        return new CartResponse(
                cartId,
                CartStatus.ACTIVE.name(),
                List.of(itemResponse),
                2,
                new BigDecimal("2599.98")
        );
    }

    public static AddToCartRequest addToCartRequest(UUID productId, int quantity) {
        return new AddToCartRequest(productId, quantity);
    }

    public static UpdateCartItemRequest updateCartItemRequest(int quantity) {
        return new UpdateCartItemRequest(quantity);
    }
}
