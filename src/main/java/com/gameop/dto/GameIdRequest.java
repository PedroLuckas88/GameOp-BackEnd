package com.gameop.dto;

import jakarta.validation.constraints.NotBlank;

public record GameIdRequest(@NotBlank(message = "O gameId é obrigatório.") String gameId) {}
