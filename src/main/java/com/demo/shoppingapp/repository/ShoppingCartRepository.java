package com.demo.shoppingapp.repository;

import com.demo.shoppingapp.model.CartStatus;
import com.demo.shoppingapp.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    @Query("SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product " +
            "WHERE c.userId = :userId AND c.status = :status")
    Optional<ShoppingCart> findByUserIdAndStatusWithItems(
            @Param("userId") String userId,
            @Param("status") CartStatus status
    );

    Optional<ShoppingCart> findByUserIdAndStatus(String userId, CartStatus status);

    boolean existsByUserIdAndStatus(String userId, CartStatus status);
}
