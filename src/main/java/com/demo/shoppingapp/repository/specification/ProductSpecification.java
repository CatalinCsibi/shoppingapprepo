package com.demo.shoppingapp.repository.specification;

import com.demo.shoppingapp.dto.request.ProductSearchRequest;
import com.demo.shoppingapp.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> withFilters(ProductSearchRequest request) {
        return Specification
                .where(isActive())
                .and(hasNameLike(request.query()))
                .and(hasCategory(request.category()))
                .and(hasPriceBetween(request.minPrice(), request.maxPrice()))
                .and(isInStock(request.inStock()));
    }

    private static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    private static Specification<Product> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    private static Specification<Product> hasCategory(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isBlank()) return null;
            return cb.equal(cb.lower(root.get("category")), category.toLowerCase());
        };
    }

    private static Specification<Product> hasPriceBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) {
                return cb.between(root.get("price"),
                        BigDecimal.valueOf(min), BigDecimal.valueOf(max));
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(min));
            }
            return cb.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(max));
        };
    }

    private static Specification<Product> isInStock(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null || !inStock) return null;
            return cb.greaterThan(root.get("stockQuantity"), 0);
        };
    }
}
