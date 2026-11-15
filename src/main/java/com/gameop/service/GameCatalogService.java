package com.gameop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gameop.dto.GamePageResponse;
import com.gameop.dto.GameResponse;
import com.gameop.exception.ApiException;
import com.gameop.igdb.IgdbClient;
import com.gameop.igdb.IgdbGameMapper;
import com.gameop.igdb.IgdbQueries;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GameCatalogService {

    private final IgdbClient igdbClient;
    private final IgdbGameMapper mapper;
    private final int catalogSize;
    private final Cache<String, List<GameResponse>> catalogCache;

    private volatile List<GameResponse> lastSuccessfulCatalog = List.of();

    public GameCatalogService(
            IgdbClient igdbClient,
            IgdbGameMapper mapper,
            @Value("${igdb.catalog-size}") int catalogSize,
            @Value("${igdb.cache-ttl-minutes}") long cacheTtlMinutes) {
        this.igdbClient = igdbClient;
        this.mapper = mapper;
        this.catalogSize = catalogSize;
        this.catalogCache = Caffeine.newBuilder()
                .maximumSize(4)
                .expireAfterWrite(Duration.ofMinutes(cacheTtlMinutes))
                .build();
    }

    public List<GameResponse> getCatalog() {
        return catalogCache.get("catalog", key -> loadCatalog());
    }

    public GamePageResponse list(int page, int size, String sort) {
        List<GameResponse> catalog = safeCatalog();
        List<GameResponse> sorted = sortCatalog(catalog, sort);
        return paginate(sorted, page, size);
    }

    public List<GameResponse> popular(int limit) {
        return safeCatalog().stream().limit(Math.max(1, limit)).toList();
    }

    public List<GameResponse> featured(int limit) {
        return safeCatalog().stream()
                .sorted(Comparator.comparing(GameResponse::rating, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(GameResponse::reviewCount, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(Math.max(1, limit))
                .toList();
    }

    public GameResponse getById(String id) {
        return safeCatalog().stream()
                .filter(game -> game.id().equals(id))
                .findFirst()
                .orElseGet(() -> fetchById(id));
    }

    @Cacheable(value = "gameSearch", key = "#query.toLowerCase()")
    public List<GameResponse> search(String query) {
        String q = query.trim();
        if (q.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Informe o termo de busca.");
        }
        String lower = q.toLowerCase(Locale.ROOT);
        List<GameResponse> local = safeCatalog().stream()
                .filter(game -> matches(game, lower))
                .toList();
        if (local.size() >= 8) {
            return local;
        }
        try {
            Map<String, GameResponse> merged = new LinkedHashMap<>();
            local.forEach(game -> merged.put(game.id(), game));
            JsonNode remote = igdbClient.queryGames(IgdbQueries.searchQuery(q, 40));
            mapAll(remote).forEach(game -> merged.putIfAbsent(game.id(), game));
            return List.copyOf(merged.values());
        } catch (ApiException ex) {
            if (!local.isEmpty()) {
                return local;
            }
            throw ex;
        }
    }

    public GamePageResponse byGenre(String genre, int page, int size) {
        String lower = genre.toLowerCase(Locale.ROOT);
        List<GameResponse> filtered = safeCatalog().stream()
                .filter(game -> game.genre().stream().anyMatch(g -> g.toLowerCase(Locale.ROOT).contains(lower)))
                .toList();
        return paginate(filtered, page, size);
    }

    private List<GameResponse> safeCatalog() {
        try {
            List<GameResponse> catalog = getCatalog();
            if (!catalog.isEmpty()) {
                lastSuccessfulCatalog = catalog;
            }
            return catalog;
        } catch (ApiException ex) {
            if (!lastSuccessfulCatalog.isEmpty()) {
                return lastSuccessfulCatalog;
            }
            throw ex;
        }
    }

    private List<GameResponse> loadCatalog() {
        Map<String, GameResponse> byId = new LinkedHashMap<>();
        int pageSize = 500;
        int pages = Math.max(1, (int) Math.ceil(catalogSize / (double) pageSize));
        for (int i = 0; i < pages && byId.size() < catalogSize; i++) {
            JsonNode batch = igdbClient.queryGames(IgdbQueries.popularQuery(pageSize, i * pageSize));
            mapAll(batch).forEach(game -> byId.putIfAbsent(game.id(), game));
        }

        List<String> franchises = IgdbQueries.PRIORITY_FRANCHISES;
        int chunk = 12;
        for (int i = 0; i < franchises.size(); i += chunk) {
            List<String> slice = franchises.subList(i, Math.min(i + chunk, franchises.size()));
            JsonNode extra = igdbClient.queryGames(IgdbQueries.franchiseQuery(slice, 50));
            mapAll(extra).forEach(game -> byId.putIfAbsent(game.id(), game));
        }

        List<GameResponse> catalog = new ArrayList<>(byId.values());
        catalog.sort(Comparator.comparing(GameResponse::reviewCount, Comparator.nullsLast(Comparator.reverseOrder())));
        if (catalog.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "A IGDB não retornou jogos utilizáveis.");
        }
        lastSuccessfulCatalog = List.copyOf(catalog);
        return lastSuccessfulCatalog;
    }

    private GameResponse fetchById(String id) {
        long igdbId;
        try {
            igdbId = Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Jogo não encontrado.");
        }
        JsonNode node = igdbClient.queryGames(IgdbQueries.byIdQuery(igdbId));
        List<GameResponse> mapped = mapAll(node);
        if (mapped.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Jogo não encontrado.");
        }
        return mapped.get(0);
    }

    private List<GameResponse> mapAll(JsonNode array) {
        List<GameResponse> games = new ArrayList<>();
        if (array == null || !array.isArray()) {
            return games;
        }
        array.forEach(node -> {
            GameResponse game = mapper.toResponse(node);
            if (game != null) {
                games.add(game);
            }
        });
        return games;
    }

    private static boolean matches(GameResponse game, String lower) {
        if (game.title() != null && game.title().toLowerCase(Locale.ROOT).contains(lower)) {
            return true;
        }
        if (game.developer() != null && game.developer().toLowerCase(Locale.ROOT).contains(lower)) {
            return true;
        }
        return game.franchise() != null && game.franchise().toLowerCase(Locale.ROOT).contains(lower);
    }

    private static List<GameResponse> sortCatalog(List<GameResponse> catalog, String sort) {
        if ("rating".equalsIgnoreCase(sort)) {
            return catalog.stream()
                    .sorted(Comparator.comparing(GameResponse::rating, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }
        return catalog;
    }

    private static GamePageResponse paginate(List<GameResponse> source, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        int from = Math.min(safePage * safeSize, source.size());
        int to = Math.min(from + safeSize, source.size());
        int totalPages = source.isEmpty() ? 0 : (int) Math.ceil(source.size() / (double) safeSize);
        return new GamePageResponse(source.subList(from, to), safePage, safeSize, source.size(), totalPages);
    }
}
