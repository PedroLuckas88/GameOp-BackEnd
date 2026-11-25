package com.gameop.repository;

import com.gameop.entity.GameList;
import com.gameop.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameListRepository extends JpaRepository<GameList, UUID> {

    @EntityGraph(attributePaths = {"user", "items"})
    List<GameList> findByUserOrderByUpdatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "items"})
    List<GameList> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items"})
    @Query("SELECT g FROM GameList g WHERE g.id = :id")
    Optional<GameList> findWithDetailsById(@Param("id") UUID id);
}
