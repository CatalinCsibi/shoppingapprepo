package com.demo.shoppingapp.controller;

import com.demo.shoppingapp.config.KeycloakJwtConverter;
import com.demo.shoppingapp.config.SecurityConfig;
import com.demo.shoppingapp.dto.request.CreateProductRequest;
import com.demo.shoppingapp.dto.response.PagedResponse;
import com.demo.shoppingapp.dto.response.ProductResponse;
import com.demo.shoppingapp.exception.GlobalExceptionHandler;
import com.demo.shoppingapp.exception.ResourceNotFoundException;
import com.demo.shoppingapp.service.ProductService;
import com.demo.shoppingapp.support.TestCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, TestCacheConfig.class})
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private KeycloakJwtConverter keycloakJwtConverter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void searchProductsIsPublicAndReturnsPagedApiResponse() throws Exception {
        ProductResponse response = new ProductResponse(
                UUID.randomUUID(),
                "iPhone 15",
                "Latest Apple smartphone",
                new BigDecimal("1299.99"),
                20,
                "phones",
                "https://example.com/iphone.png",
                true,
                LocalDateTime.of(2025, 1, 1, 12, 0)
        );
        when(productService.searchProducts(any()))
                .thenReturn(new PagedResponse<>(List.of(response), 0, 20, 1, 1, true, true, false, false));

        mockMvc.perform(get("/api/v1/products").param("query", "iphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("iPhone 15"));

        verify(productService).searchProducts(any());
    }

    @Test
    void getProductReturnsNotFoundWhenTheServiceThrows() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id))
                .thenThrow(new ResourceNotFoundException("Product", "id", id));

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found with id: '" + id + "'"));
    }

    @Test
    void createProductRequiresTheAdminRole() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Laptop",
                "Description",
                new BigDecimal("1999.99"),
                3,
                "computers",
                "https://example.com/laptop.png"
        );

        mockMvc.perform(post("/api/v1/products")
                        .with(jwt())
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Laptop",
                                  "description": "Description",
                                  "price": 1999.99,
                                  "stockQuantity": 3,
                                  "category": "computers",
                                  "imageUrl": "https://example.com/laptop.png"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProductReturnsCreatedWhenCalledByAnAdmin() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Laptop",
                "Description",
                new BigDecimal("1999.99"),
                3,
                "computers",
                "https://example.com/laptop.png"
        );
        ProductResponse response = new ProductResponse(
                UUID.randomUUID(),
                "Laptop",
                "Description",
                new BigDecimal("1999.99"),
                3,
                "computers",
                "https://example.com/laptop.png",
                true,
                LocalDateTime.of(2025, 1, 1, 12, 0)
        );
        when(productService.createProduct(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Laptop",
                                  "description": "Description",
                                  "price": 1999.99,
                                  "stockQuantity": 3,
                                  "category": "computers",
                                  "imageUrl": "https://example.com/laptop.png"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.name").value("Laptop"));

        verify(productService).createProduct(eq(request));
    }

    @Test
    void createProductReturnsBadRequestWhenValidationFails() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "",
                "Description",
                new BigDecimal("1999.99"),
                3,
                "computers",
                "https://example.com/laptop.png"
        );

        mockMvc.perform(post("/api/v1/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "",
                                  "description": "Description",
                                  "price": 1999.99,
                                  "stockQuantity": 3,
                                  "category": "computers",
                                  "imageUrl": "https://example.com/laptop.png"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.name").exists());
    }
}
