CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   email VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   profile_picture VARCHAR(255),
   designation VARCHAR(50),
   birthdate DATE,
   join_date DATE,
   predicted_burnout_risk BOOLEAN
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_designation ON users(designation);

CREATE TABLE blacklisted_tokens (
   id BIGSERIAL PRIMARY KEY,
   token VARCHAR(255) NOT NULL UNIQUE,
   blacklisted_at TIMESTAMP NOT NULL,
   expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_blacklisted_tokens_token ON blacklisted_tokens(token);
CREATE INDEX idx_blacklisted_tokens_expires_at ON blacklisted_tokens(expires_at);

CREATE TABLE refresh_tokens (
   id BIGSERIAL PRIMARY KEY,
   token VARCHAR(255) NOT NULL UNIQUE,
   expiry_date TIMESTAMP NOT NULL,
   user_id BIGINT NOT NULL,
   user_agent VARCHAR(255) NOT NULL,
   ip_address VARCHAR(45) NOT NULL,
   created_at TIMESTAMP NOT NULL,
   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);