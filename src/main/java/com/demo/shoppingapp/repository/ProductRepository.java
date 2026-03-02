package com.demo.shoppingapp.repository;

import com.demo.shoppingapp.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndActiveTrue(UUID id);

    Page<Product> findByActiveTrue(Pageable pageable);

    // Full-text search with PostgreSQL
    @Query(value = """
        SELECT * FROM products p 
        WHERE p.active = true 
        AND (
            p.name ILIKE %:query% 
            OR p.description ILIKE %:query%
        )
        """,
            countQuery = "SELECT COUNT(*) FROM products p WHERE p.active = true AND (p.name ILIKE %:query% OR p.description ILIKE %:query%)",
            nativeQuery = true)
    Page<Product> searchByNameOrDescription(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.active = true")
    List<String> findAllCategories();

    boolean existsByNameIgnoreCase(String name);
}
