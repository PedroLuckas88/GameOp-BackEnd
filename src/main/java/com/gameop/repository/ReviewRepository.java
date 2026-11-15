package com.gameop.repository;

import com.gameop.entity.Review;
import com.gameop.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByUserOrderByUpdatedAtDesc(User user);

    List<Review> findByIgdbGameIdOrderByUpdatedAtDesc(Long igdbGameId);

    Optional<Review> findByUserAndIgdbGameId(User user, Long igdbGameId);

    List<Review> findTop6ByOrderByUpdatedAtDesc();
}
