alter table customer
    add column if not exists preferred_payment_method_code varchar(64),
    add column if not exists preferred_shipping_method_code varchar(64),
    add column if not exists privacy_accepted_at timestamp with time zone,
    add column if not exists privacy_policy_version varchar(64);

alter table customer_address
    add column if not exists address_type varchar(32) not null default 'SHIPPING';

update customer_address
set address_type = 'SHIPPING'
where address_type is null or btrim(address_type) = '';

create index if not exists idx_customer_address_customer_type
    on customer_address(customer_id, address_type);
