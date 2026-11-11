CREATE TABLE loans (
    id              UUID            PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES users(id),
    amount          DECIMAL(19,2)   NOT NULL CHECK (amount > 0),
    term_months     INTEGER         NOT NULL CHECK (term_months > 0),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    reviewed_at     TIMESTAMPTZ,
    reviewed_by     UUID            REFERENCES users(id),
    rejection_reason VARCHAR(500),
    version         BIGINT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_loans_user_id ON loans(user_id);
CREATE INDEX idx_loans_status ON loans(status);
