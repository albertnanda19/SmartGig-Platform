CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    event_key VARCHAR(100),
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP
);

CREATE INDEX idx_outbox_events_published_at ON outbox_events(published_at);
CREATE INDEX idx_outbox_events_created_at ON outbox_events(created_at DESC);

