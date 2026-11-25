package com.gameop.repository;

import com.gameop.entity.Review;
import com.gameop.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @EntityGraph(attributePaths = "user")
    List<Review> findByUserOrderByUpdatedAtDesc(User user);

    @EntityGraph(attributePaths = "user")
    List<Review> findByIgdbGameIdOrderByUpdatedAtDesc(Long igdbGameId);

    Optional<Review> findByUserAndIgdbGameId(User user, Long igdbGameId);

    @EntityGraph(attributePaths = "user")
    List<Review> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    @Query("SELECT AVG(r.rating) AS average, COUNT(r) AS count FROM Review r WHERE r.igdbGameId = :gameId")
    ReviewStats statsByGameId(@Param("gameId") Long gameId);

    interface ReviewStats {
        Double getAverage();

        long getCount();
    }
}
