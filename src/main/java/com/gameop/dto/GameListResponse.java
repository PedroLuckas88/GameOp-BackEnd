package com.gameop.dto;

import com.gameop.entity.GameList;
import com.gameop.entity.GameListItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GameListResponse(
        UUID id,
        UUID userId,
        String username,
        String name,
        String description,
        List<String> gameIds,
        Instant createdAt,
        Instant updatedAt) {

    public static GameListResponse from(GameList list) {
        List<String> gameIds = list.getItems().stream()
                .map(GameListItem::getIgdbGameId)
                .map(String::valueOf)
                .toList();
        return new GameListResponse(
                list.getId(),
                list.getUser().getId(),
                list.getUser().getUsername(),
                list.getName(),
                list.getDescription(),
                gameIds,
                list.getCreatedAt(),
                list.getUpdatedAt());
    }
}
