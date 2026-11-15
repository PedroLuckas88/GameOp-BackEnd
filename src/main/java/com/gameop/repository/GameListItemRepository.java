package com.gameop.repository;

import com.gameop.entity.GameList;
import com.gameop.entity.GameListItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameListItemRepository extends JpaRepository<GameListItem, UUID> {

    Optional<GameListItem> findByGameListAndIgdbGameId(GameList gameList, Long igdbGameId);

    boolean existsByGameListAndIgdbGameId(GameList gameList, Long igdbGameId);

    void deleteByGameListAndIgdbGameId(GameList gameList, Long igdbGameId);

    long countByGameList(GameList gameList);
}
