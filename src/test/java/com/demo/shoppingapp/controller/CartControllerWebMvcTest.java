package com.demo.shoppingapp.controller;

import com.demo.shoppingapp.config.KeycloakJwtConverter;
import com.demo.shoppingapp.config.SecurityConfig;
import com.demo.shoppingapp.dto.request.AddToCartRequest;
import com.demo.shoppingapp.dto.response.CartResponse;
import com.demo.shoppingapp.exception.GlobalExceptionHandler;
import com.demo.shoppingapp.service.CartService;
import com.demo.shoppingapp.support.TestCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.demo.shoppingapp.support.TestDataFactory.cartResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, TestCacheConfig.class})
class CartControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private KeycloakJwtConverter keycloakJwtConverter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void cartEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCartUsesTheJwtSubjectAsTheUserId() throws Exception {
        CartResponse response = cartResponse(UUID.randomUUID());
        when(cartService.getOrCreateCart("user-123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/cart")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "user-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(cartService).getOrCreateCart("user-123");
    }

    @Test
    void addToCartReturnsBadRequestWhenValidationFails() throws Exception {
        AddToCartRequest request = new AddToCartRequest(UUID.randomUUID(), 0);

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "user-123")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": "%s",
                                  "quantity": 0
                                }
                                """.formatted(request.productId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.quantity").exists());
    }

    @Test
    void clearCartReturnsSuccessForAuthenticatedUsers() throws Exception {
        mockMvc.perform(delete("/api/v1/cart")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "user-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart cleared"));

        verify(cartService).clearCart("user-123");
    }
}
