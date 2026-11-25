package com.gameop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GameListRequest(
        @NotBlank(message = "O nome da lista é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String name,
        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String description,
        @NotEmpty(message = "A lista precisa ter pelo menos um jogo.")
        List<String> gameIds) {}
