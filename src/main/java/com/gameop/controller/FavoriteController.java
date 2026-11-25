package com.gameop.controller;

import com.gameop.dto.FavoriteResponse;
import com.gameop.dto.GameIdRequest;
import com.gameop.dto.InGamelistResponse;
import com.gameop.entity.User;
import com.gameop.service.FavoriteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gamelist")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(favoriteService.list(user));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<InGamelistResponse> contains(
            @AuthenticationPrincipal User user, @PathVariable String gameId) {
        return ResponseEntity.ok(favoriteService.contains(user, gameId));
    }

    @PostMapping
    public ResponseEntity<FavoriteResponse> add(
            @AuthenticationPrincipal User user, @Valid @RequestBody GameIdRequest request) {
        FavoriteService.AddResult result = favoriteService.add(user, request.gameId());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.favorite());
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal User user, @PathVariable String gameId) {
        favoriteService.remove(user, gameId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/toggle")
    public ResponseEntity<InGamelistResponse> toggle(
            @AuthenticationPrincipal User user, @Valid @RequestBody GameIdRequest request) {
        return ResponseEntity.ok(favoriteService.toggle(user, request.gameId()));
    }
}
