package com.events.eventmanagement.dto;

import com.events.eventmanagement.entity.User;
import com.events.eventmanagement.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean enabled,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.isEnabled(), user.getCreatedAt());
    }
}
