package com.gameop.service;

import com.gameop.dto.FavoriteResponse;
import com.gameop.dto.InGamelistResponse;
import com.gameop.entity.Favorite;
import com.gameop.entity.User;
import com.gameop.exception.ApiException;
import com.gameop.repository.FavoriteRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> list(User user) {
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InGamelistResponse contains(User user, String gameId) {
        return new InGamelistResponse(favoriteRepository.existsByUserAndIgdbGameId(user, IgdbIdParser.parse(gameId)));
    }

    @Transactional
    public AddResult add(User user, String gameId) {
        long igdbId = IgdbIdParser.parse(gameId);
        return favoriteRepository
                .findByUserAndIgdbGameId(user, igdbId)
                .map(existing -> new AddResult(FavoriteResponse.from(existing), false))
                .orElseGet(() -> {
                    Favorite created = favoriteRepository.save(
                            Favorite.builder().user(user).igdbGameId(igdbId).build());
                    return new AddResult(FavoriteResponse.from(created), true);
                });
    }

    @Transactional
    public void remove(User user, String gameId) {
        long igdbId = IgdbIdParser.parse(gameId);
        if (!favoriteRepository.existsByUserAndIgdbGameId(user, igdbId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Jogo não está na gamelist.");
        }
        favoriteRepository.deleteByUserAndIgdbGameId(user, igdbId);
    }

    @Transactional
    public InGamelistResponse toggle(User user, String gameId) {
        long igdbId = IgdbIdParser.parse(gameId);
        if (favoriteRepository.existsByUserAndIgdbGameId(user, igdbId)) {
            favoriteRepository.deleteByUserAndIgdbGameId(user, igdbId);
            return new InGamelistResponse(false);
        }
        favoriteRepository.save(Favorite.builder().user(user).igdbGameId(igdbId).build());
        return new InGamelistResponse(true);
    }

    public record AddResult(FavoriteResponse favorite, boolean created) {}
}
