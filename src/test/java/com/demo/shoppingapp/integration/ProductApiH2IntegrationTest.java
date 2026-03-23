package com.demo.shoppingapp.integration;

import com.demo.shoppingapp.dto.request.CreateProductRequest;
import com.demo.shoppingapp.model.CartStatus;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.repository.ProductRepository;
import com.demo.shoppingapp.repository.ShoppingCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
class ProductApiH2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @BeforeEach
    void setUp() {
        shoppingCartRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void searchProductsReturnsPublicResultsFromTheEmbeddedDatabase() throws Exception {
        Product product = Product.builder()
                .name("iPhone 15")
                .description("Latest Apple smartphone")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(20)
                .category("phones")
                .imageUrl("https://example.com/iphone.png")
                .active(true)
                .build();
        productRepository.save(product);

        mockMvc.perform(get("/api/v1/products").param("query", "iphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("iPhone 15"));
    }

    @Test
    void createProductPersistsDataThroughTheFullStackOnH2() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "MacBook Pro",
                "High-end laptop",
                new BigDecimal("2499.99"),
                5,
                "laptops",
                "https://example.com/macbook.png"
        );

        mockMvc.perform(post("/api/v1/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "MacBook Pro",
                                  "description": "High-end laptop",
                                  "price": 2499.99,
                                  "stockQuantity": 5,
                                  "category": "laptops",
                                  "imageUrl": "https://example.com/macbook.png"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("MacBook Pro"));

        assertThat(productRepository.existsByNameIgnoreCase("MacBook Pro")).isTrue();
    }

    @Test
    void getCartCreatesACartForAnAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "user-42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        assertThat(shoppingCartRepository.findAll())
                .hasSize(1)
                .first()
                .extracting(cart -> cart.getStatus())
                .isEqualTo(CartStatus.ACTIVE);
    }
}
