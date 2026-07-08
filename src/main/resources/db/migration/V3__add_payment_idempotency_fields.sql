ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stripe_checkout_url TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_idempotency_key
    ON payments (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_stripe_checkout_session_id
    ON payments (stripe_checkout_session_id)
    WHERE stripe_checkout_session_id IS NOT NULL;
