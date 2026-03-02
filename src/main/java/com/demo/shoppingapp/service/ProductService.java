package com.demo.shoppingapp.service;

import com.demo.shoppingapp.dto.request.CreateProductRequest;
import com.demo.shoppingapp.dto.request.ProductSearchRequest;
import com.demo.shoppingapp.dto.response.PagedResponse;
import com.demo.shoppingapp.dto.response.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    PagedResponse<ProductResponse> searchProducts(ProductSearchRequest request);

    ProductResponse getProductById(UUID id);

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(UUID id, CreateProductRequest request);

    void deleteProduct(UUID id);

    List<String> getAllCategories();
}
