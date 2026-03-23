package com.demo.shoppingapp.repository;

import com.demo.shoppingapp.model.CartItem;
import com.demo.shoppingapp.model.CartStatus;
import com.demo.shoppingapp.model.Product;
import com.demo.shoppingapp.model.ShoppingCart;
import com.demo.shoppingapp.support.TestCacheConfig;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.UUID;

import static com.demo.shoppingapp.support.TestDataFactory.cartItem;
import static com.demo.shoppingapp.support.TestDataFactory.product;
import static com.demo.shoppingapp.support.TestDataFactory.shoppingCart;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestCacheConfig.class)
class ShoppingCartRepositoryDataJpaTest {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        shoppingCartRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void fetchJoinQueryLoadsItemsAndProductsForTheCart() {
        Product product = product();
        product.setId(null);
        product.setVersion(null);
        productRepository.saveAndFlush(product);

        ShoppingCart cart = shoppingCart("user-1");
        cart.setId(null);
        CartItem item = cartItem(cart, product, 2);
        item.setId(null);
        cart.addItem(item);
        shoppingCartRepository.saveAndFlush(cart);

        entityManager.clear();

        Optional<ShoppingCart> result = shoppingCartRepository.findByUserIdAndStatusWithItems("user-1", CartStatus.ACTIVE);

        assertThat(result).isPresent();
        assertThat(Hibernate.isInitialized(result.orElseThrow().getItems())).isTrue();
        assertThat(result.orElseThrow().getItems()).hasSize(1);
        assertThat(Hibernate.isInitialized(result.orElseThrow().getItems().getFirst().getProduct())).isTrue();
    }

    @Test
    void existsByUserIdAndStatusMatchesPersistedCarts() {
        ShoppingCart cart = shoppingCart("user-1");
        cart.setId(null);
        shoppingCartRepository.saveAndFlush(cart);

        assertThat(shoppingCartRepository.existsByUserIdAndStatus("user-1", CartStatus.ACTIVE)).isTrue();
        assertThat(shoppingCartRepository.existsByUserIdAndStatus("missing", CartStatus.ACTIVE)).isFalse();
    }
}
