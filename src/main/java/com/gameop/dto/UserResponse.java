package com.gameop.dto;

import com.gameop.entity.User;
import java.util.UUID;

public record UserResponse(UUID id, String username, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
