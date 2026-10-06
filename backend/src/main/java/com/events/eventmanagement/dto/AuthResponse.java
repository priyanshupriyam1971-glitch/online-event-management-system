package com.events.eventmanagement.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {}
