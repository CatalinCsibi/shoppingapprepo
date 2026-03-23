package com.demo.shoppingapp.mapper;

import com.demo.shoppingapp.dto.request.CreateProductRequest;
import com.demo.shoppingapp.dto.response.ProductResponse;
import com.demo.shoppingapp.model.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toResponseMarksProductInStockWhenQuantityIsPositive() {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Phone")
                .price(new BigDecimal("999.99"))
                .stockQuantity(7)
                .build();

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response.inStock()).isTrue();
    }

    @Test
    void toEntityInitializesManagedFieldsForNewProducts() {
        CreateProductRequest request = new CreateProductRequest(
                "Phone",
                "Flagship",
                new BigDecimal("999.99"),
                10,
                "phones",
                "https://example.com/phone.png"
        );

        Product product = productMapper.toEntity(request);

        assertThat(product.getId()).isNull();
        assertThat(product.getVersion()).isNull();
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
        assertThat(product.getActive()).isTrue();
        assertThat(product.getName()).isEqualTo("Phone");
    }

    @Test
    void updateEntityFromRequestIgnoresNullValues() {
        Product product = Product.builder()
                .name("Phone")
                .description("Original description")
                .price(new BigDecimal("999.99"))
                .stockQuantity(5)
                .category("phones")
                .imageUrl("https://example.com/original.png")
                .active(true)
                .build();

        CreateProductRequest request = new CreateProductRequest(
                "Updated Phone",
                null,
                null,
                null,
                "premium-phones",
                null
        );

        productMapper.updateEntityFromRequest(request, product);

        assertThat(product.getName()).isEqualTo("Updated Phone");
        assertThat(product.getDescription()).isEqualTo("Original description");
        assertThat(product.getPrice()).isEqualByComparingTo("999.99");
        assertThat(product.getStockQuantity()).isEqualTo(5);
        assertThat(product.getCategory()).isEqualTo("premium-phones");
        assertThat(product.getImageUrl()).isEqualTo("https://example.com/original.png");
    }
}
