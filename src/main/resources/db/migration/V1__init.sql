CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    keycloak_user_id VARCHAR(64) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(128),
    last_name VARCHAR(128),
    phone VARCHAR(32),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE customer_address (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    line_1 VARCHAR(255) NOT NULL,
    line_2 VARCHAR(255),
    city VARCHAR(128) NOT NULL,
    region VARCHAR(128),
    country VARCHAR(128) NOT NULL,
    postal_code VARCHAR(32) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_customer_address_customer
        FOREIGN KEY (customer_id) REFERENCES customer(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_customer_address_customer ON customer_address(customer_id);
