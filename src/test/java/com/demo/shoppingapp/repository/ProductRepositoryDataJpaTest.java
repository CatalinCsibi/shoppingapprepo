package com.demo.shoppingapp.repository;

import com.demo.shoppingapp.dto.request.ProductSearchRequest;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.repository.specification.ProductSpecification;
import com.demo.shoppingapp.support.TestCacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static com.demo.shoppingapp.support.TestDataFactory.inactiveProduct;
import static com.demo.shoppingapp.support.TestDataFactory.product;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestCacheConfig.class)
class ProductRepositoryDataJpaTest {

    @Autowired
    private ProductRepository productRepository;

    private Product phone;
    private Product headphones;
    private Product inactivePhone;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        phone = product();
        phone.setId(null);
        phone.setVersion(null);
        phone.setName("iPhone 15");

        headphones = product();
        headphones.setId(null);
        headphones.setVersion(null);
        headphones.setName("Sony Headphones");
        headphones.setStockQuantity(0);
        headphones.setCategory("audio");
        headphones.setPrice(headphones.getPrice().subtract(new java.math.BigDecimal("900.00")));
        inactivePhone = inactiveProduct(UUID.randomUUID(), "Old iPhone");
        inactivePhone.setId(null);
        inactivePhone.setVersion(null);

        productRepository.saveAll(List.of(phone, headphones, inactivePhone));
    }

    @Test
    void findByIdAndActiveTrueIgnoresInactiveProducts() {
        assertThat(productRepository.findByIdAndActiveTrue(phone.getId())).contains(phone);
        assertThat(productRepository.findByIdAndActiveTrue(inactivePhone.getId())).isEmpty();
    }

    @Test
    void specificationFilteringCombinesTheOptionalSearchCriteria() {
        ProductSearchRequest request = new ProductSearchRequest(
                "iphone",
                "phones",
                1000.0,
                1500.0,
                true,
                0,
                20,
                "name",
                "ASC"
        );

        Page<Product> result = productRepository.findAll(
                ProductSpecification.withFilters(request),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"))
        );

        assertThat(result.getContent()).containsExactly(phone);
    }

    @Test
    void findAllCategoriesReturnsDistinctActiveCategoriesOnly() {
        List<String> categories = productRepository.findAllCategories();

        assertThat(categories).containsExactlyInAnyOrder("phones", "audio");
    }

    @Test
    void existsByNameIgnoreCaseMatchesRegardlessOfCase() {
        assertThat(productRepository.existsByNameIgnoreCase("iphone 15")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("IPHONE 15")).isTrue();
        assertThat(productRepository.existsByNameIgnoreCase("Nonexistent")).isFalse();
    }
}
