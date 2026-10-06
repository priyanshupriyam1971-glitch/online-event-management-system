package com.events.eventmanagement.controller;

import com.events.eventmanagement.dto.RegistrationRequest;
import com.events.eventmanagement.dto.RegistrationResponse;
import com.events.eventmanagement.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/events/{eventId}/register")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable Long eventId,
            @Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.register(eventId, request));
    }

    @GetMapping("/api/attendees/registrations")
    public List<RegistrationResponse> myRegistrations() {
        return registrationService.listMyRegistrations();
    }

    @GetMapping("/api/organizers/events/{eventId}/registrations")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<RegistrationResponse> eventRegistrations(@PathVariable Long eventId) {
        return registrationService.listEventRegistrations(eventId);
    }
}
