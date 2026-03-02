-- Enable UUID extension (required for UUID PKs)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================
-- PRODUCTS TABLE
-- ================================
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category VARCHAR(255),
    image_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

-- Useful index for searching
CREATE INDEX idx_products_name ON products (LOWER(name));
CREATE INDEX idx_products_category ON products (LOWER(category));


-- ================================
-- SHOPPING CARTS TABLE
-- ================================
CREATE TABLE shopping_carts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cart_user ON shopping_carts (user_id);
CREATE INDEX idx_cart_status ON shopping_carts (status);


-- ================================
-- CART ITEMS TABLE
-- ================================
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1),
    price_at_addition NUMERIC(10, 2) NOT NULL,
    
    CONSTRAINT fk_cart FOREIGN KEY (cart_id) 
        REFERENCES shopping_carts (id) ON DELETE CASCADE,

    CONSTRAINT fk_product FOREIGN KEY (product_id) 
        REFERENCES products (id),

    CONSTRAINT unique_cart_product UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_items_cart ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product ON cart_items (product_id);
