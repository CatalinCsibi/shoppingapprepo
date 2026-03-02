-- Timestamp trigger function
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for products
CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- Trigger for shopping_carts
CREATE TRIGGER trg_carts_updated_at
    BEFORE UPDATE ON shopping_carts
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();
