CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(255) PRIMARY KEY,
    amount DECIMAL(19, 2),
    currency VARCHAR(10),
    status VARCHAR(50),
    order_id VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refunds (
    id VARCHAR(255) PRIMARY KEY,
    payment_id VARCHAR(255),
    amount DECIMAL(19, 2),
    status VARCHAR(50),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    key_value VARCHAR(255) PRIMARY KEY,
    response_body TEXT,
    response_status INT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS webhook_logs (
    id UUID PRIMARY KEY,
    event_type VARCHAR(50),
    payload TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP
);
