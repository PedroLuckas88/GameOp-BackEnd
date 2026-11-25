package com.gameop.dto;

import com.gameop.entity.Favorite;
import java.time.Instant;
import java.util.UUID;

public record FavoriteResponse(UUID id, String gameId, UUID userId, Instant addedAt) {

    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                String.valueOf(favorite.getIgdbGameId()),
                favorite.getUser().getId(),
                favorite.getCreatedAt());
    }
}
