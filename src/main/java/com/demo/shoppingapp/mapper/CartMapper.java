package com.demo.shoppingapp.mapper;

import com.demo.shoppingapp.dto.cart.CartItemResponse;
import com.demo.shoppingapp.dto.cart.CartResponse;
import com.demo.shoppingapp.model.CartItem;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitPrice", source = "priceAtTime")
    @Mapping(target = "lineTotal", expression = "java(cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity())))")
    CartItemResponse toCartItemResponse(CartItem cartItem, Product product);

    List<CartItemResponse> toCartItemResponses(List<CartItem> items);

    CartResponse toCartResponse(
            ShoppingCart cart,
            List<CartItemResponse> items,
            BigDecimal total
    );
}