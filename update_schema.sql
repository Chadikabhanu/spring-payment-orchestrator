-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Add webhook_secret to merchants
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS webhook_secret VARCHAR(64);
-- Set a test secret for development
UPDATE merchants SET webhook_secret = 'whsec_test_abc123' WHERE email = 'test@example.com';

-- 2. Create Refunds Table
CREATE TABLE IF NOT EXISTS refunds (
    id VARCHAR(64) PRIMARY KEY,
    payment_id VARCHAR(64) NOT NULL REFERENCES payments(id),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    amount INTEGER NOT NULL,
    reason TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- 3. Create Webhook Logs Table
CREATE TABLE IF NOT EXISTS webhook_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    event VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    attempts INTEGER DEFAULT 0,
    last_attempt_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    response_code INTEGER,
    response_body TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create Idempotency Keys Table
CREATE TABLE IF NOT EXISTS idempotency_keys (
    key VARCHAR(255),
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    response JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    PRIMARY KEY (key, merchant_id)
);

-- 5. Add captured column to payments
ALTER TABLE payments ADD COLUMN IF NOT EXISTS captured BOOLEAN DEFAULT FALSE;