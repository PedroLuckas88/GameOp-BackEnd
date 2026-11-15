package com.gameop.igdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.gameop.dto.GameResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class IgdbGameMapper {

    public GameResponse toResponse(JsonNode node) {
        if (node == null || node.path("id").isMissingNode() || node.path("name").asText("").isBlank()) {
            return null;
        }
        String coverId = node.path("cover").path("image_id").asText(null);
        if (coverId == null || coverId.isBlank()) {
            return null;
        }

        List<String> screenshots = new ArrayList<>();
        node.path("screenshots").forEach(shot -> {
            String imageId = shot.path("image_id").asText(null);
            if (imageId != null && !imageId.isBlank()) {
                screenshots.add(imageUrl("screenshot_huge", imageId));
            }
        });

        String developer = null;
        String publisher = null;
        for (JsonNode company : node.path("involved_companies")) {
            String name = company.path("company").path("name").asText(null);
            if (Boolean.TRUE.equals(asBoolean(company.path("developer"))) && developer == null) {
                developer = name;
            }
            if (Boolean.TRUE.equals(asBoolean(company.path("publisher"))) && publisher == null) {
                publisher = name;
            }
        }

        List<String> genres = new ArrayList<>();
        node.path("genres").forEach(g -> {
            String name = g.path("name").asText(null);
            if (name != null) {
                genres.add(name);
            }
        });

        List<String> platforms = new ArrayList<>();
        node.path("platforms").forEach(p -> {
            String name = p.path("name").asText(null);
            if (name != null) {
                platforms.add(name);
            }
        });

        String franchise = firstName(node.path("franchises"));
        if (franchise == null) {
            franchise = node.path("collection").path("name").asText(null);
        }

        String trailer = null;
        JsonNode videos = node.path("videos");
        if (videos.isArray() && !videos.isEmpty()) {
            String videoId = videos.get(0).path("video_id").asText(null);
            if (videoId != null && !videoId.isBlank()) {
                trailer = "https://www.youtube.com/watch?v=" + videoId;
            }
        }

        Integer year = null;
        long unix = node.path("first_release_date").asLong(0);
        if (unix > 0) {
            year = Instant.ofEpochSecond(unix).atZone(ZoneOffset.UTC).getYear();
        }

        double igdbRating = node.path("total_rating").asDouble(node.path("aggregated_rating").asDouble(0));
        Double rating = igdbRating > 0 ? Math.round(igdbRating / 20.0 * 10.0) / 10.0 : null;
        int reviewCount = node.path("total_rating_count").asInt(node.path("aggregated_rating_count").asInt(0));

        String background = screenshots.isEmpty() ? imageUrl("screenshot_huge", coverId) : screenshots.get(0);

        return new GameResponse(
                String.valueOf(node.get("id").asLong()),
                node.get("name").asText(),
                developer,
                publisher,
                year,
                List.copyOf(genres),
                imageUrl("cover_big_2x", coverId),
                background,
                blankToNull(node.path("summary").asText(null)),
                rating,
                reviewCount,
                List.copyOf(platforms),
                List.copyOf(screenshots),
                trailer,
                franchise);
    }

    private static String imageUrl(String size, String imageId) {
        return "https://images.igdb.com/igdb/image/upload/t_" + size + "/" + imageId + ".jpg";
    }

    private static String firstName(JsonNode array) {
        if (array.isArray() && !array.isEmpty()) {
            String name = array.get(0).path("name").asText(null);
            return blankToNull(name);
        }
        return null;
    }

    private static Boolean asBoolean(JsonNode node) {
        return node.isMissingNode() ? null : node.asBoolean();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
