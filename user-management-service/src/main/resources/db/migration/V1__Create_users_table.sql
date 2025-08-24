-- /main/resources/db/migration/V1__Create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    -- email VARCHAR(255) NOT NULL UNIQUE,

    email VARCHAR(255) NOT NULL ,
    password VARCHAR(255) NOT NULL,
    profile_picture_url TEXT,
    profile_picture_data BYTEA,
    designation VARCHAR(50),
    birthdate DATE,
    join_date DATE,
    predicted_burnout_risk BOOLEAN
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_designation ON users(designation);