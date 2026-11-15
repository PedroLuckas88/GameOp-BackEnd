package com.gameop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "O username é obrigatório.")
        @Size(min = 2, max = 50, message = "O username deve ter entre 2 e 50 caracteres.")
        String username,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 4, message = "A senha deve ter pelo menos 4 caracteres.")
        String password) {
}
