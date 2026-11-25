package com.gameop.service;

import com.gameop.dto.GameIdRequest;
import com.gameop.dto.GameListRequest;
import com.gameop.dto.GameListResponse;
import com.gameop.entity.GameList;
import com.gameop.entity.GameListItem;
import com.gameop.entity.User;
import com.gameop.exception.ApiException;
import com.gameop.repository.GameListRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameListService {

    private final GameListRepository gameListRepository;

    public GameListService(GameListRepository gameListRepository) {
        this.gameListRepository = gameListRepository;
    }

    @Transactional(readOnly = true)
    public List<GameListResponse> recent(int limit) {
        int size = Math.min(50, Math.max(1, limit));
        return gameListRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(0, size)).stream()
                .map(GameListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameListResponse> listMine(User user) {
        return gameListRepository.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(GameListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameListResponse getById(UUID listId) {
        return GameListResponse.from(requireList(listId));
    }

    @Transactional
    public GameListResponse create(User user, GameListRequest request) {
        GameList list = GameList.builder()
                .user(user)
                .name(request.name().trim())
                .description(blankToNull(request.description()))
                .build();
        uniqueIds(request.gameIds()).forEach(id -> list.getItems().add(item(list, id)));
        return GameListResponse.from(gameListRepository.save(list));
    }

    @Transactional
    public GameListResponse update(User user, UUID listId, GameListRequest request) {
        GameList list = requireOwned(user, listId);
        list.setName(request.name().trim());
        list.setDescription(blankToNull(request.description()));
        list.getItems().clear();
        uniqueIds(request.gameIds()).forEach(id -> list.getItems().add(item(list, id)));
        return GameListResponse.from(gameListRepository.save(list));
    }

    @Transactional
    public void delete(User user, UUID listId) {
        gameListRepository.delete(requireOwned(user, listId));
    }

    @Transactional
    public GameListResponse addGame(User user, UUID listId, GameIdRequest request) {
        GameList list = requireOwned(user, listId);
        long igdbId = IgdbIdParser.parse(request.gameId());
        boolean exists = list.getItems().stream().anyMatch(item -> item.getIgdbGameId().equals(igdbId));
        if (!exists) {
            list.getItems().add(item(list, igdbId));
        }
        return GameListResponse.from(gameListRepository.save(list));
    }

    @Transactional
    public GameListResponse removeGame(User user, UUID listId, String gameId) {
        GameList list = requireOwned(user, listId);
        long igdbId = IgdbIdParser.parse(gameId);
        boolean removed = list.getItems().removeIf(item -> item.getIgdbGameId().equals(igdbId));
        if (!removed) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Jogo não está nesta lista.");
        }
        if (list.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A lista não pode ficar sem jogos.");
        }
        return GameListResponse.from(gameListRepository.save(list));
    }

    private GameList requireList(UUID listId) {
        return gameListRepository
                .findWithDetailsById(listId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lista não encontrada."));
    }

    private GameList requireOwned(User user, UUID listId) {
        GameList list = requireList(listId);
        if (!list.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acesso negado.");
        }
        return list;
    }

    private static List<Long> uniqueIds(List<String> gameIds) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        gameIds.forEach(id -> ids.add(IgdbIdParser.parse(id)));
        if (ids.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A lista precisa ter pelo menos um jogo.");
        }
        return List.copyOf(ids);
    }

    private static GameListItem item(GameList list, long igdbId) {
        return GameListItem.builder().gameList(list).igdbGameId(igdbId).build();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
