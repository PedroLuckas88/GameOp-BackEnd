package com.gameop.dto;

import java.util.List;

public record GameResponse(
        String id,
        String title,
        String developer,
        String publisher,
        Integer releaseYear,
        List<String> genre,
        String coverImage,
        String backgroundImage,
        String description,
        Double rating,
        Integer reviewCount,
        List<String> platforms,
        List<String> screenshots,
        String trailerUrl,
        String franchise) {}
