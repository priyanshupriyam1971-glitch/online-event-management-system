package com.events.eventmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(
        @NotBlank String title,
        @NotBlank String content
) {}
