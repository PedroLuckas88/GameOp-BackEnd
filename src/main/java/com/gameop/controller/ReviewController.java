package com.gameop.controller;

import com.gameop.dto.ReviewRequest;
import com.gameop.dto.ReviewResponse;
import com.gameop.dto.ReviewStatsResponse;
import com.gameop.entity.User;
import com.gameop.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/games/{gameId}/reviews")
    public ResponseEntity<List<ReviewResponse>> byGame(@PathVariable String gameId) {
        return ResponseEntity.ok(reviewService.listByGame(gameId));
    }

    @GetMapping("/api/games/{gameId}/reviews/stats")
    public ResponseEntity<ReviewStatsResponse> stats(@PathVariable String gameId) {
        return ResponseEntity.ok(reviewService.stats(gameId));
    }

    @GetMapping("/api/reviews/me")
    public ResponseEntity<List<ReviewResponse>> mine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.listMine(user));
    }

    @GetMapping("/api/reviews/recent")
    public ResponseEntity<List<ReviewResponse>> recent(@RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(reviewService.recent(limit));
    }

    @PutMapping("/api/games/{gameId}/reviews")
    public ResponseEntity<ReviewResponse> upsert(
            @AuthenticationPrincipal User user,
            @PathVariable String gameId,
            @Valid @RequestBody ReviewRequest request) {
        ReviewService.UpsertResult result = reviewService.upsert(user, gameId, request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.review());
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID reviewId) {
        reviewService.delete(user, reviewId);
        return ResponseEntity.noContent().build();
    }
}
