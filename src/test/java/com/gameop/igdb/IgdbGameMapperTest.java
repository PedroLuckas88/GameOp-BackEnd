package com.gameop.igdb;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameop.dto.GameResponse;
import org.junit.jupiter.api.Test;

class IgdbGameMapperTest {

    private final IgdbGameMapper mapper = new IgdbGameMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void mapsPopularGameAndSkipsMissingCover() throws Exception {
        var withCover = objectMapper.readTree("""
                {
                  "id": 1942,
                  "name": "The Witcher 3: Wild Hunt",
                  "cover": { "image_id": "abc" },
                  "summary": "RPG",
                  "total_rating": 93.4,
                  "total_rating_count": 2000,
                  "first_release_date": 1431993600,
                  "genres": [{ "name": "Role-playing (RPG)" }],
                  "platforms": [{ "name": "PC" }],
                  "involved_companies": [
                    { "company": { "name": "CD Projekt RED" }, "developer": true, "publisher": false }
                  ],
                  "franchises": [{ "name": "The Witcher" }],
                  "videos": [{ "video_id": "xyz" }]
                }
                """);
        GameResponse game = mapper.toResponse(withCover);
        assertThat(game).isNotNull();
        assertThat(game.id()).isEqualTo("1942");
        assertThat(game.title()).contains("Witcher");
        assertThat(game.coverImage()).contains("cover_big_2x");
        assertThat(game.rating()).isEqualTo(4.7);
        assertThat(game.developer()).isEqualTo("CD Projekt RED");
        assertThat(game.trailerUrl()).contains("youtube");

        var noCover = objectMapper.readTree("""
                { "id": 1, "name": "Sem capa" }
                """);
        assertThat(mapper.toResponse(noCover)).isNull();
    }
}
