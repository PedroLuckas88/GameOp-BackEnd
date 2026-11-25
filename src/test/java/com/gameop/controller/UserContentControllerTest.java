package com.gameop.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameop.dto.GameIdRequest;
import com.gameop.dto.GameListRequest;
import com.gameop.dto.RegisterRequest;
import com.gameop.dto.ReviewRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reviewsFavoritesAndListsFollowContract() throws Exception {
        String token = register("lucia", "lucia@example.com");

        mockMvc.perform(put("/api/games/1942/reviews")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReviewRequest(5, "Excelente."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.gameId").value("1942"));

        mockMvc.perform(get("/api/games/1942/reviews")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/games/1942/reviews/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.average").value(5.0));
        mockMvc.perform(get("/api/reviews/recent")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(post("/api/gamelist/toggle")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameIdRequest("1942"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inGamelist").value(true));

        mockMvc.perform(get("/api/gamelist").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        String createdList = mockMvc.perform(post("/api/lists")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GameListRequest("Rockstar", "Western", List.of("1020")))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String listId = objectMapper.readTree(createdList).get("id").asText();

        mockMvc.perform(get("/api/lists/recent")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/lists/" + listId)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Rockstar"));

        mockMvc.perform(get("/api/gamelist")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/lists/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void ownerOnlyCanEditListAndReview() throws Exception {
        String owner = register("dona", "dona@example.com");
        String other = register("outra", "outra@example.com");

        JsonNode review = objectMapper.readTree(mockMvc.perform(put("/api/games/1905/reviews")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReviewRequest(4, null))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(delete("/api/reviews/" + review.get("id").asText())
                        .header("Authorization", bearer(other)))
                .andExpect(status().isForbidden());

        JsonNode list = objectMapper.readTree(mockMvc.perform(post("/api/lists")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GameListRequest("Minha", null, List.of("1", "2")))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(delete("/api/lists/" + list.get("id").asText())
                        .header("Authorization", bearer(other)))
                .andExpect(status().isForbidden());
    }

    private String register(String username, String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(username, email, "abcd"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
