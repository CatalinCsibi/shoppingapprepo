package com.demo.shoppingapp.mapper;

import com.demo.shoppingapp.dto.product.ProductRequest;
import com.demo.shoppingapp.dto.product.ProductResponse;
import com.demo.shoppingapp.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product mapToProduct(ProductRequest request);

    ProductResponse mapToProductResponse(Product product);
}