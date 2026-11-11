CREATE INDEX IF NOT EXISTS idx_loans_user_id_status ON loans(user_id, status);
CREATE INDEX IF NOT EXISTS idx_loans_user_id_created_at ON loans(user_id, created_at DESC);
