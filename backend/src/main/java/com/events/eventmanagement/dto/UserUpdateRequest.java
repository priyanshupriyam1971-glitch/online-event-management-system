package com.events.eventmanagement.dto;

import com.events.eventmanagement.enums.Role;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank String name,
        Role role,
        Boolean enabled
) {}
