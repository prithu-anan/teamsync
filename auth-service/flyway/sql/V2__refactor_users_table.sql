-- Migration to refactor users table to minimal authentication schema

-- Add new columns
ALTER TABLE users ADD COLUMN is_active BOOLEAN;
ALTER TABLE users ADD COLUMN created_at TIMESTAMP;
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;

-- Set default values
UPDATE users SET is_active = TRUE;
UPDATE users SET created_at = CURRENT_TIMESTAMP;

-- Remove old columns
ALTER TABLE users DROP COLUMN profile_picture;
ALTER TABLE users DROP COLUMN designation;
ALTER TABLE users DROP COLUMN birthdate;
ALTER TABLE users DROP COLUMN join_date;
ALTER TABLE users DROP COLUMN predicted_burnout_risk;

-- Create new index
CREATE INDEX idx_users_is_active ON users(is_active);
