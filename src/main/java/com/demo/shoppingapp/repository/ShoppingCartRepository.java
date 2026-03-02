package com.demo.shoppingapp.repository;

import com.demo.shoppingapp.model.CartStatus;
import com.demo.shoppingapp.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    Optional<ShoppingCart> findByUserIdAndStatus(String userId, CartStatus status);
}