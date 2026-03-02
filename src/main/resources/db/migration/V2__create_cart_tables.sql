CREATE TABLE shopping_cart
(
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE cart_items
(
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_time NUMERIC(10,2) NOT NULL,

    CONSTRAINT fk_cart
        FOREIGN KEY (cart_id)
            REFERENCES shopping_cart(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
);