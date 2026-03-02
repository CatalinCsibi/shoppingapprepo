package com.demo.shoppingapp.service.impl;

import com.demo.shoppingapp.dto.request.CreateProductRequest;
import com.demo.shoppingapp.dto.request.ProductSearchRequest;
import com.demo.shoppingapp.dto.response.PagedResponse;
import com.demo.shoppingapp.dto.response.ProductResponse;
import com.demo.shoppingapp.exception.BusinessException;
import com.demo.shoppingapp.exception.ResourceNotFoundException;
import com.demo.shoppingapp.mapper.ProductMapper;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.repository.ProductRepository;
import com.demo.shoppingapp.repository.specification.ProductSpecification;
import com.demo.shoppingapp.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public PagedResponse<ProductResponse> searchProducts(ProductSearchRequest request) {
        log.debug("Searching products with criteria: {}", request);

        Sort sort = Sort.by(
                Sort.Direction.fromString(request.sortDirection()),
                request.sortBy()
        );
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<Product> productPage = productRepository.findAll(
                ProductSpecification.withFilters(request),
                pageable
        );

        Page<ProductResponse> responsePage = productPage.map(productMapper::toResponse);
        return PagedResponse.from(responsePage);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(UUID id) {
        log.debug("Fetching product by id: {}", id);

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.name());

        if (productRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Product with this name already exists");
        }

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        log.info("Product created with id: {}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(UUID id, CreateProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productMapper.updateEntityFromRequest(request, product);
        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(UUID id) {
        log.info("Soft deleting product: {}", id);

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Cacheable(value = "categories")
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
}
