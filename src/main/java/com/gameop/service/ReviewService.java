package com.gameop.service;

import com.gameop.dto.ReviewRequest;
import com.gameop.dto.ReviewResponse;
import com.gameop.dto.ReviewStatsResponse;
import com.gameop.entity.Review;
import com.gameop.entity.User;
import com.gameop.exception.ApiException;
import com.gameop.repository.ReviewRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listByGame(String gameId) {
        return reviewRepository.findByIgdbGameIdOrderByUpdatedAtDesc(IgdbIdParser.parse(gameId)).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewStatsResponse stats(String gameId) {
        ReviewRepository.ReviewStats stats = reviewRepository.statsByGameId(IgdbIdParser.parse(gameId));
        long count = stats == null ? 0 : stats.getCount();
        Double average = count == 0 ? null : round(stats.getAverage());
        return new ReviewStatsResponse(average, count);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listMine(User user) {
        return reviewRepository.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> recent(int limit) {
        int size = Math.min(50, Math.max(1, limit));
        return reviewRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, size)).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public UpsertResult upsert(User user, String gameId, ReviewRequest request) {
        long igdbId = IgdbIdParser.parse(gameId);
        return reviewRepository
                .findByUserAndIgdbGameId(user, igdbId)
                .map(existing -> {
                    existing.setRating(request.rating());
                    existing.setComment(blankToNull(request.comment()));
                    return new UpsertResult(ReviewResponse.from(reviewRepository.save(existing)), false);
                })
                .orElseGet(() -> {
                    Review created = reviewRepository.save(Review.builder()
                            .user(user)
                            .igdbGameId(igdbId)
                            .rating(request.rating())
                            .comment(blankToNull(request.comment()))
                            .build());
                    return new UpsertResult(ReviewResponse.from(created), true);
                });
    }

    @Transactional
    public void delete(User user, UUID reviewId) {
        Review review = reviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Avaliação não encontrada."));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        reviewRepository.delete(review);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static Double round(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10.0) / 10.0;
    }

    public record UpsertResult(ReviewResponse review, boolean created) {}
}
