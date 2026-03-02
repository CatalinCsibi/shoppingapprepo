package com.demo.shoppingapp.repository;

import com.demo.shoppingapp.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    void deleteByCartIdAndProductId(UUID cartId, UUID productId);
}
