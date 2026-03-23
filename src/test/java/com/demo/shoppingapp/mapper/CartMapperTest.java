package com.demo.shoppingapp.mapper;

import com.demo.shoppingapp.dto.response.CartItemResponse;
import com.demo.shoppingapp.dto.response.CartResponse;
import com.demo.shoppingapp.model.CartItem;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.model.ShoppingCart;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static com.demo.shoppingapp.support.TestDataFactory.cartItem;
import static com.demo.shoppingapp.support.TestDataFactory.product;
import static com.demo.shoppingapp.support.TestDataFactory.shoppingCart;
import static org.assertj.core.api.Assertions.assertThat;

class CartMapperTest {

    private final CartMapper cartMapper = Mappers.getMapper(CartMapper.class);

    @Test
    void toResponseMapsItemsAndCartTotals() {
        ShoppingCart cart = shoppingCart("user-1");
        Product product = product();
        CartItem item = cartItem(cart, product, 2);
        cart.addItem(item);

        CartResponse response = cartMapper.toResponse(cart);

        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.totalItems()).isEqualTo(2);
        assertThat(response.totalPrice()).isEqualByComparingTo("2599.98");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productName()).isEqualTo(product.getName());
    }

    @Test
    void toItemResponseUsesProductAndPriceSnapshotData() {
        ShoppingCart cart = shoppingCart("user-1");
        Product product = product();
        CartItem item = cartItem(cart, product, 3);
        item.setPriceAtAddition(new BigDecimal("100.00"));

        CartItemResponse response = cartMapper.toItemResponse(item);

        assertThat(response.productId()).isEqualTo(product.getId());
        assertThat(response.productName()).isEqualTo(product.getName());
        assertThat(response.unitPrice()).isEqualByComparingTo("100.00");
        assertThat(response.subtotal()).isEqualByComparingTo("300.00");
    }
}
