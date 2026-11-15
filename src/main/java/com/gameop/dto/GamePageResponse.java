package com.gameop.dto;

import java.util.List;

public record GamePageResponse(
        List<GameResponse> content, int page, int size, long totalElements, int totalPages) {}
