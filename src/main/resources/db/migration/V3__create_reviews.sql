CREATE TABLE reviews (
    id              UUID            NOT NULL,
    user_id         UUID            NOT NULL,
    igdb_game_id    BIGINT          NOT NULL,
    rating          SMALLINT        NOT NULL,
    comment         VARCHAR(1000),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_reviews_user_game UNIQUE (user_id, igdb_game_id),
    CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_reviews_user_id ON reviews (user_id);
CREATE INDEX idx_reviews_igdb_game_id ON reviews (igdb_game_id);
CREATE INDEX idx_reviews_updated_at ON reviews (updated_at);
