CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP
);

CREATE TABLE tokens (
                        id BIGSERIAL PRIMARY KEY,
                        token TEXT NOT NULL UNIQUE,
                        token_type VARCHAR(20) NOT NULL,
                        revoked BOOLEAN NOT NULL DEFAULT FALSE,
                        user_id BIGINT NOT NULL,
                        created_at TIMESTAMP NOT NULL,


                        CONSTRAINT fk_tokens_user
                            FOREIGN KEY (user_id)
                            REFERENCES users(id)
                            ON DELETE CASCADE

);

CREATE INDEX idx_tokens_token ON tokens(token);
