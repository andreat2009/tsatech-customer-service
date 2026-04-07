ALTER TABLE customer
    ADD COLUMN IF NOT EXISTS customer_group_code VARCHAR(64) NOT NULL DEFAULT 'RETAIL';

UPDATE customer
SET customer_group_code = 'RETAIL'
WHERE customer_group_code IS NULL OR customer_group_code = '';

CREATE INDEX IF NOT EXISTS idx_customer_group_code ON customer(customer_group_code);
