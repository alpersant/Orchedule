CREATE TABLE app_user (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          email VARCHAR(180) NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,
                          full_name VARCHAR(150) NOT NULL,
                          role VARCHAR(30) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          CONSTRAINT uq_app_user_email UNIQUE (email),
                          CONSTRAINT ck_app_user_email_not_blank CHECK (length(trim(email)) > 5),
                          CONSTRAINT ck_app_user_name_not_blank CHECK (length(trim(full_name)) >= 2),
                          CONSTRAINT ck_app_user_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE refresh_token (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL,
                               token_hash VARCHAR(255) NOT NULL,
                               expires_at TIMESTAMPTZ NOT NULL,
                               revoked BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
                                   REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_app_user_email ON app_user(email);
CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_token_hash ON refresh_token(token_hash);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token(expires_at);