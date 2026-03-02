package com.demo.shoppingapp.mapper;

import com.demo.shoppingapp.dto.cart.CartItemResponse;
import com.demo.shoppingapp.model.CartItem;
import com.demo.shoppingapp.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitPrice", source = "cartItem.priceAtTime")
    @Mapping(target = "lineTotal",
            expression = "java(cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity())))")
    CartItemResponse mapToCartItemResponse(CartItem cartItem, Product product);
}