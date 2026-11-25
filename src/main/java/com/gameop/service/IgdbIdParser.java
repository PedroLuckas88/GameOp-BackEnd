package com.gameop.service;

import com.gameop.exception.ApiException;
import org.springframework.http.HttpStatus;

final class IgdbIdParser {

    private IgdbIdParser() {}

    static long parse(String gameId) {
        try {
            return Long.parseLong(gameId);
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Identificador de jogo inválido.");
        }
    }
}
