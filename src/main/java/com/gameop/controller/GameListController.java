package com.gameop.controller;

import com.gameop.dto.GameIdRequest;
import com.gameop.dto.GameListRequest;
import com.gameop.dto.GameListResponse;
import com.gameop.entity.User;
import com.gameop.service.GameListService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lists")
public class GameListController {

    private final GameListService gameListService;

    public GameListController(GameListService gameListService) {
        this.gameListService = gameListService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<GameListResponse>> recent(@RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(gameListService.recent(limit));
    }

    @GetMapping("/me")
    public ResponseEntity<List<GameListResponse>> mine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gameListService.listMine(user));
    }

    @GetMapping("/{listId}")
    public ResponseEntity<GameListResponse> byId(@PathVariable UUID listId) {
        return ResponseEntity.ok(gameListService.getById(listId));
    }

    @PostMapping
    public ResponseEntity<GameListResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody GameListRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameListService.create(user, request));
    }

    @PutMapping("/{listId}")
    public ResponseEntity<GameListResponse> update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID listId,
            @Valid @RequestBody GameListRequest request) {
        return ResponseEntity.ok(gameListService.update(user, listId, request));
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID listId) {
        gameListService.delete(user, listId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{listId}/games")
    public ResponseEntity<GameListResponse> addGame(
            @AuthenticationPrincipal User user,
            @PathVariable UUID listId,
            @Valid @RequestBody GameIdRequest request) {
        return ResponseEntity.ok(gameListService.addGame(user, listId, request));
    }

    @DeleteMapping("/{listId}/games/{gameId}")
    public ResponseEntity<GameListResponse> removeGame(
            @AuthenticationPrincipal User user, @PathVariable UUID listId, @PathVariable String gameId) {
        return ResponseEntity.ok(gameListService.removeGame(user, listId, gameId));
    }
}
