-- SPDX-License-Identifier: AGPL-3.0-or-later
--
-- Users and refresh tokens for email + password authentication.

CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    id            uuid        PRIMARY KEY DEFAULT uuidv7(),
    -- citext makes case-insensitivity a property of the column, so no query can
    -- bypass it by forgetting to lower() one side of a comparison.
    email         citext      NOT NULL UNIQUE,
    password_hash text        NOT NULL,
    created_at    timestamptz NOT NULL DEFAULT now(),
    updated_at    timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT users_email_length CHECK (length(email) BETWEEN 3 AND 254)
);

CREATE TABLE refresh_tokens (
    id          uuid        PRIMARY KEY DEFAULT uuidv7(),
    user_id     uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    -- SHA-256 of the opaque token, hex encoded. The token itself is never
    -- stored: a dump of this table must not yield usable credentials.
    token_hash  char(64)    NOT NULL UNIQUE,
    issued_at   timestamptz NOT NULL DEFAULT now(),
    expires_at  timestamptz NOT NULL,
    -- Set when the token is rotated away or explicitly revoked. Presenting a
    -- token that already has this set means replay or theft.
    revoked_at  timestamptz,
    -- The token issued in this one's place, forming the rotation chain.
    replaced_by uuid        REFERENCES refresh_tokens (id) ON DELETE SET NULL
);

-- Revoking every token for a user, which reuse detection does on every hit.
CREATE INDEX refresh_tokens_user_id_idx ON refresh_tokens (user_id);

-- Supports a future sweep of expired rows. Partial because revoked rows are
-- reached through the user_id index and need no separate expiry scan.
CREATE INDEX refresh_tokens_expires_at_idx
    ON refresh_tokens (expires_at)
    WHERE revoked_at IS NULL;
