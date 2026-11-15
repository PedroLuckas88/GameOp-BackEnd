CREATE TABLE favorites (
    id              UUID            NOT NULL,
    user_id         UUID            NOT NULL,
    igdb_game_id    BIGINT          NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_favorites PRIMARY KEY (id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_favorites_user_game UNIQUE (user_id, igdb_game_id)
);

CREATE INDEX idx_favorites_user_id ON favorites (user_id);
