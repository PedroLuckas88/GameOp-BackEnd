CREATE TABLE game_lists (
    id              UUID            NOT NULL,
    user_id         UUID            NOT NULL,
    name            VARCHAR(120)    NOT NULL,
    description     VARCHAR(1000),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_game_lists PRIMARY KEY (id),
    CONSTRAINT fk_game_lists_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_game_lists_user_id ON game_lists (user_id);
CREATE INDEX idx_game_lists_updated_at ON game_lists (updated_at);

CREATE TABLE game_list_items (
    id              UUID            NOT NULL,
    game_list_id    UUID            NOT NULL,
    igdb_game_id    BIGINT          NOT NULL,
    added_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_game_list_items PRIMARY KEY (id),
    CONSTRAINT fk_game_list_items_list FOREIGN KEY (game_list_id) REFERENCES game_lists (id) ON DELETE CASCADE,
    CONSTRAINT uk_game_list_items_list_game UNIQUE (game_list_id, igdb_game_id)
);

CREATE INDEX idx_game_list_items_list_id ON game_list_items (game_list_id);
