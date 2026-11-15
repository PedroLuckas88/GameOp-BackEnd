package com.gameop.repository;

import com.gameop.entity.GameList;
import com.gameop.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameListRepository extends JpaRepository<GameList, UUID> {

    List<GameList> findByUserOrderByUpdatedAtDesc(User user);

    List<GameList> findTop6ByOrderByUpdatedAtDesc();
}
