package com.demo.shoppingapp.service.impl;

import com.demo.shoppingapp.dto.request.AddToCartRequest;
import com.demo.shoppingapp.dto.request.UpdateCartItemRequest;
import com.demo.shoppingapp.dto.response.CartResponse;
import com.demo.shoppingapp.exception.BusinessException;
import com.demo.shoppingapp.exception.ResourceNotFoundException;
import com.demo.shoppingapp.mapper.CartMapper;
import com.demo.shoppingapp.model.CartItem;
import com.demo.shoppingapp.model.CartStatus;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.model.ShoppingCart;
import com.demo.shoppingapp.repository.CartItemRepository;
import com.demo.shoppingapp.repository.ProductRepository;
import com.demo.shoppingapp.repository.ShoppingCartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static com.demo.shoppingapp.support.TestDataFactory.addToCartRequest;
import static com.demo.shoppingapp.support.TestDataFactory.cartItem;
import static com.demo.shoppingapp.support.TestDataFactory.cartResponse;
import static com.demo.shoppingapp.support.TestDataFactory.product;
import static com.demo.shoppingapp.support.TestDataFactory.shoppingCart;
import static com.demo.shoppingapp.support.TestDataFactory.updateCartItemRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ShoppingCartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void getOrCreateCartReturnsTheExistingActiveCart() {
        ShoppingCart cart = shoppingCart("user-1");
        CartResponse response = cartResponse(cart.getId());

        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartMapper.toResponse(cart)).thenReturn(response);

        CartResponse result = cartService.getOrCreateCart("user-1");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getOrCreateCartCreatesANewCartWhenMissing() {
        ShoppingCart savedCart = shoppingCart("user-1");
        CartResponse response = cartResponse(savedCart.getId());

        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(ShoppingCart.class))).thenReturn(savedCart);
        when(cartMapper.toResponse(savedCart)).thenReturn(response);

        CartResponse result = cartService.getOrCreateCart("user-1");

        assertThat(result).isEqualTo(response);
        verify(cartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void addToCartRejectsRequestsWhenStockIsInsufficient() {
        Product product = product();
        product.setStockQuantity(1);
        AddToCartRequest request = addToCartRequest(product.getId(), 2);

        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addToCart("user-1", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");

        verify(cartRepository, never()).save(any(ShoppingCart.class));
    }

    @Test
    void addToCartCreatesANewCartItemWhenTheProductIsNotAlreadyPresent() {
        Product product = product();
        ShoppingCart cart = shoppingCart("user-1");
        AddToCartRequest request = addToCartRequest(product.getId(), 2);
        CartResponse response = cartResponse(cart.getId());

        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
                .thenReturn(Optional.empty());
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(response);

        CartResponse result = cartService.addToCart("user-1", request);

        assertThat(result).isEqualTo(response);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(cart.getItems().getFirst().getProduct()).isEqualTo(product);
    }

    @Test
    void addToCartIncrementsTheQuantityOfAnExistingItem() {
        Product product = product();
        ShoppingCart cart = shoppingCart("user-1");
        CartItem existingItem = cartItem(cart, product, 1);
        cart.addItem(existingItem);
        AddToCartRequest request = addToCartRequest(product.getId(), 2);
        CartResponse response = cartResponse(cart.getId());

        when(productRepository.findByIdAndActiveTrue(product.getId())).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
                .thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(existingItem)).thenReturn(existingItem);
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(response);

        CartResponse result = cartService.addToCart("user-1", request);

        assertThat(result).isEqualTo(response);
        assertThat(existingItem.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void updateCartItemDeletesTheItemWhenQuantityBecomesZero() {
        Product product = product();
        ShoppingCart cart = shoppingCart("user-1");
        CartItem item = cartItem(cart, product, 2);
        cart.addItem(item);
        CartResponse response = cartResponse(cart.getId());

        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(response);

        CartResponse result = cartService.updateCartItem("user-1", item.getId(), updateCartItemRequest(0));

        assertThat(result).isEqualTo(response);
        assertThat(cart.getItems()).isEmpty();
        verify(cartItemRepository).delete(item);
    }

    @Test
    void updateCartItemRejectsQuantitiesThatExceedStock() {
        Product product = product();
        product.setStockQuantity(2);
        ShoppingCart cart = shoppingCart("user-1");
        CartItem item = cartItem(cart, product, 1);
        cart.addItem(item);
        UpdateCartItemRequest request = updateCartItemRequest(3);

        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateCartItem("user-1", item.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exceeds available stock");
    }

    @Test
    void removeFromCartDeletesTheRequestedItem() {
        Product product = product();
        ShoppingCart cart = shoppingCart("user-1");
        CartItem item = cartItem(cart, product, 2);
        cart.addItem(item);
        CartResponse response = cartResponse(cart.getId());

        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toResponse(cart)).thenReturn(response);

        CartResponse result = cartService.removeFromCart("user-1", item.getId());

        assertThat(result).isEqualTo(response);
        assertThat(cart.getItems()).isEmpty();
        verify(cartItemRepository).delete(item);
    }

    @Test
    void clearCartEmptiesTheCartAndMarksItAsAbandoned() {
        Product product = product();
        ShoppingCart cart = shoppingCart("user-1");
        cart.addItem(cartItem(cart, product, 2));

        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.clearCart("user-1");

        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ABANDONED);
    }

    @Test
    void removeFromCartThrowsWhenCartDoesNotExist() {
        UUID itemId = UUID.randomUUID();
        when(cartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeFromCart("user-1", itemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart not found");
    }
}
