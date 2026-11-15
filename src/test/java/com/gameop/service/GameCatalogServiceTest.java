package com.gameop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameop.dto.GameResponse;
import com.gameop.igdb.IgdbClient;
import com.gameop.igdb.IgdbGameMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameCatalogServiceTest {

    @Mock
    private IgdbClient igdbClient;

    private GameCatalogService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new GameCatalogService(igdbClient, new IgdbGameMapper(), 500, 360);
    }

    @Test
    void loadsCatalogDeduplicatesAndSearches() throws Exception {
        var json = objectMapper.readTree("""
                [
                  {
                    "id": 1020,
                    "name": "Grand Theft Auto V",
                    "cover": { "image_id": "gta" },
                    "total_rating": 90,
                    "total_rating_count": 3000,
                    "genres": [{ "name": "Shooter" }],
                    "involved_companies": [
                      { "company": { "name": "Rockstar" }, "developer": true, "publisher": true }
                    ]
                  },
                  {
                    "id": 1942,
                    "name": "God of War",
                    "cover": { "image_id": "gow" },
                    "total_rating": 96,
                    "total_rating_count": 2500,
                    "genres": [{ "name": "Adventure" }]
                  }
                ]
                """);
        when(igdbClient.queryGames(anyString())).thenReturn(json);

        var page = service.list(0, 10, null);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).extracting(GameResponse::title).contains("Grand Theft Auto V", "God of War");

        assertThat(service.search("gta")).extracting(GameResponse::title).contains("Grand Theft Auto V");
        assertThat(service.getById("1942").title()).isEqualTo("God of War");
        assertThat(service.byGenre("Adventure", 0, 10).content()).hasSize(1);
        assertThat(service.popular(1)).hasSize(1);
        assertThat(service.featured(2)).hasSize(2);
    }
}
