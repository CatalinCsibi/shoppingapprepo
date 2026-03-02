package com.demo.shoppingapp.mapper;

import com.demo.shoppingapp.dto.response.CartItemResponse;
import com.demo.shoppingapp.dto.response.CartResponse;
import com.demo.shoppingapp.model.CartItem;
import com.demo.shoppingapp.model.ShoppingCart;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartResponse toResponse(ShoppingCart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.imageUrl")
    @Mapping(target = "unitPrice", source = "priceAtAddition")
    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    CartItemResponse toItemResponse(CartItem item);

    List<CartItemResponse> toItemResponseList(List<CartItem> items);
}
