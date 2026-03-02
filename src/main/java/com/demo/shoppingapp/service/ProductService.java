package com.demo.shoppingapp.service;

import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        log.info("Finding all products");
        return repository.findAll(pageable);
    }

    public Page<Product> findProductByName(String name, Pageable pageable) {
        log.info("Finding product by name: {}", name);
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Product createProduct(Product product) {
        log.info("Creating object by name: {}", product.getName());
        return repository.save(product);
    }
}
