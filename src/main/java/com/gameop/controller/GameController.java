package com.gameop.controller;

import com.gameop.dto.GamePageResponse;
import com.gameop.dto.GameResponse;
import com.gameop.service.GameCatalogService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameCatalogService gameCatalogService;

    public GameController(GameCatalogService gameCatalogService) {
        this.gameCatalogService = gameCatalogService;
    }

    @GetMapping
    public ResponseEntity<GamePageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(gameCatalogService.list(page, size, sort));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<GameResponse>> popular(@RequestParam(defaultValue = "24") int limit) {
        return ResponseEntity.ok(gameCatalogService.popular(limit));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<GameResponse>> featured(@RequestParam(defaultValue = "12") int limit) {
        return ResponseEntity.ok(gameCatalogService.featured(limit));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GameResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(gameCatalogService.search(q));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<GamePageResponse> byGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return ResponseEntity.ok(gameCatalogService.byGenre(genre, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> byId(@PathVariable String id) {
        return ResponseEntity.ok(gameCatalogService.getById(id));
    }
}
