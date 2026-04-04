CREATE TABLE custom_field_definition (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL,
    placeholder VARCHAR(255),
    help_text VARCHAR(1000),
    field_type VARCHAR(32) NOT NULL,
    field_scope VARCHAR(32) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    persist_for_customer BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE custom_field_option (
    id BIGSERIAL PRIMARY KEY,
    custom_field_id BIGINT NOT NULL,
    option_value VARCHAR(128) NOT NULL,
    label VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_custom_field_option_definition
        FOREIGN KEY (custom_field_id) REFERENCES custom_field_definition(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_custom_field_option_value UNIQUE (custom_field_id, option_value)
);

CREATE TABLE customer_custom_field_value (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    custom_field_id BIGINT NOT NULL,
    field_value VARCHAR(4000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_customer_custom_field_value_customer
        FOREIGN KEY (customer_id) REFERENCES customer(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_customer_custom_field_value_definition
        FOREIGN KEY (custom_field_id) REFERENCES custom_field_definition(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_customer_custom_field UNIQUE (customer_id, custom_field_id)
);

CREATE INDEX idx_custom_field_scope_active ON custom_field_definition(field_scope, active, sort_order);
CREATE INDEX idx_customer_custom_field_customer ON customer_custom_field_value(customer_id);
