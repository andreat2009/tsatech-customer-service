CREATE TABLE customer_wishlist (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_customer_wishlist_customer
        FOREIGN KEY (customer_id) REFERENCES customer(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_customer_wishlist UNIQUE (customer_id, product_id)
);

CREATE INDEX idx_customer_wishlist_customer ON customer_wishlist(customer_id);
CREATE INDEX idx_customer_wishlist_product ON customer_wishlist(product_id);
