package com.gameop.igdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.gameop.exception.ApiException;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TwitchAuthService {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUrl;

    private final Object lock = new Object();
    private String accessToken;
    private Instant expiresAt = Instant.EPOCH;

    public TwitchAuthService(
            RestClient restClient,
            @Value("${twitch.client-id}") String clientId,
            @Value("${twitch.client-secret}") String clientSecret,
            @Value("${twitch.token-url}") String tokenUrl) {
        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUrl = tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAccessToken() {
        synchronized (lock) {
            if (accessToken != null && Instant.now().isBefore(expiresAt.minusSeconds(60))) {
                return accessToken;
            }
            refresh();
            return accessToken;
        }
    }

    private void refresh() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Credenciais da Twitch não configuradas no servidor.");
        }
        try {
            var tokenUri = UriComponentsBuilder.fromUriString(tokenUrl)
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("grant_type", "client_credentials")
                    .build(true)
                    .toUri();
            JsonNode body = restClient
                    .post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null || body.path("access_token").isMissingNode()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Falha ao autenticar na Twitch.");
            }
            accessToken = body.get("access_token").asText();
            long expiresIn = body.path("expires_in").asLong(3600);
            expiresAt = Instant.now().plusSeconds(expiresIn);
        } catch (ApiException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Não foi possível autenticar na Twitch.");
        }
    }
}
