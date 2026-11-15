package com.gameop.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gameop.entity.Favorite;
import com.gameop.entity.GameList;
import com.gameop.entity.GameListItem;
import com.gameop.entity.Review;
import com.gameop.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GameListRepository gameListRepository;

    @Autowired
    private GameListItemRepository gameListItemRepository;

    @Test
    void persistsUserOwnedDataAndRejectsDuplicateFavorite() {
        User user = userRepository.save(User.builder()
                .username("caio")
                .email("caio@example.com")
                .passwordHash("hash")
                .build());

        favoriteRepository.saveAndFlush(Favorite.builder()
                .user(user)
                .igdbGameId(1942L)
                .build());

        reviewRepository.saveAndFlush(Review.builder()
                .user(user)
                .igdbGameId(1942L)
                .rating(5)
                .comment("Excelente.")
                .build());

        GameList list = gameListRepository.saveAndFlush(GameList.builder()
                .user(user)
                .name("Rockstar")
                .description("Western e crime")
                .build());

        GameListItem item = GameListItem.builder()
                .gameList(list)
                .igdbGameId(1942L)
                .build();
        list.getItems().add(item);
        gameListRepository.saveAndFlush(list);

        assertThat(favoriteRepository.existsByUserAndIgdbGameId(user, 1942L)).isTrue();
        assertThat(reviewRepository.findByUserAndIgdbGameId(user, 1942L)).isPresent();
        assertThat(gameListItemRepository.countByGameList(list)).isEqualTo(1);

        assertThatThrownBy(() -> favoriteRepository.saveAndFlush(Favorite.builder()
                        .user(user)
                        .igdbGameId(1942L)
                        .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateReviewForSameUserAndGame() {
        User user = userRepository.save(User.builder()
                .username("ana")
                .email("ana@example.com")
                .passwordHash("hash")
                .build());

        reviewRepository.saveAndFlush(Review.builder()
                .user(user)
                .igdbGameId(1905L)
                .rating(4)
                .build());

        assertThatThrownBy(() -> reviewRepository.saveAndFlush(Review.builder()
                        .user(user)
                        .igdbGameId(1905L)
                        .rating(3)
                        .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
