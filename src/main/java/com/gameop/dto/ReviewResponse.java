package com.gameop.dto;

import com.gameop.entity.Review;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        String gameId,
        UUID userId,
        String username,
        int rating,
        String comment,
        Instant createdAt,
        Instant updatedAt) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                String.valueOf(review.getIgdbGameId()),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
