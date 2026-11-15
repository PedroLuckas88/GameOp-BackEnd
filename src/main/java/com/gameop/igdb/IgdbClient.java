package com.gameop.igdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.gameop.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class IgdbClient {

    private static final String GAME_FIELDS =
            "fields id,name,cover.image_id,screenshots.image_id,first_release_date,genres.name,"
                    + "platforms.name,involved_companies.company.name,involved_companies.developer,"
                    + "involved_companies.publisher,summary,aggregated_rating,aggregated_rating_count,"
                    + "total_rating,total_rating_count,videos.video_id,franchises.name,collection.name;";

    private final RestClient restClient;
    private final TwitchAuthService twitchAuthService;
    private final String baseUrl;

    public IgdbClient(
            RestClient restClient, TwitchAuthService twitchAuthService, @Value("${igdb.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.twitchAuthService = twitchAuthService;
        this.baseUrl = baseUrl;
    }

    public JsonNode queryGames(String apicalypseBody) {
        return post("/games", GAME_FIELDS + " " + apicalypseBody);
    }

    public JsonNode queryGamesRaw(String fullBody) {
        return post("/games", fullBody);
    }

    private JsonNode post(String path, String body) {
        try {
            JsonNode response = restClient
                    .post()
                    .uri(baseUrl + path)
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Client-ID", twitchAuthService.getClientId())
                    .header("Authorization", "Bearer " + twitchAuthService.getAccessToken())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            return response == null ? com.fasterxml.jackson.databind.node.MissingNode.getInstance() : response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Autenticação IGDB/Twitch recusada.");
            }
            if (ex.getStatusCode().value() == 429) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Limite da IGDB atingido. Tente novamente em instantes.");
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "A IGDB está indisponível no momento.");
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Não foi possível contactar a IGDB.");
        }
    }
}
