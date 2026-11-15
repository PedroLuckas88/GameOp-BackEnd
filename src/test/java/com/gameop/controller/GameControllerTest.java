package com.gameop.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gameop.dto.GamePageResponse;
import com.gameop.dto.GameResponse;
import com.gameop.service.GameCatalogService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameCatalogService gameCatalogService;

    @Test
    void exposesPublicCatalogEndpoints() throws Exception {
        GameResponse game = new GameResponse(
                "1020",
                "Grand Theft Auto V",
                "Rockstar",
                "Rockstar",
                2013,
                List.of("Shooter"),
                "https://images.igdb.com/cover.jpg",
                "https://images.igdb.com/bg.jpg",
                "Los Santos",
                4.5,
                3000,
                List.of("PlayStation 4"),
                List.of(),
                null,
                "Grand Theft Auto");
        when(gameCatalogService.list(anyInt(), anyInt(), isNull()))
                .thenReturn(new GamePageResponse(List.of(game), 0, 24, 1, 1));
        when(gameCatalogService.popular(anyInt())).thenReturn(List.of(game));
        when(gameCatalogService.search(anyString())).thenReturn(List.of(game));
        when(gameCatalogService.getById("1020")).thenReturn(game);

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Grand Theft Auto V"));
        mockMvc.perform(get("/api/games/popular")).andExpect(status().isOk());
        mockMvc.perform(get("/api/games/search").param("q", "GTA")).andExpect(status().isOk());
        mockMvc.perform(get("/api/games/1020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1020"));
    }
}
