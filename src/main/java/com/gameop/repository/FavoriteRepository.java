package com.gameop.repository;

import com.gameop.entity.Favorite;
import com.gameop.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUserOrderByCreatedAtDesc(User user);

    Optional<Favorite> findByUserAndIgdbGameId(User user, Long igdbGameId);

    boolean existsByUserAndIgdbGameId(User user, Long igdbGameId);

    void deleteByUserAndIgdbGameId(User user, Long igdbGameId);
}
