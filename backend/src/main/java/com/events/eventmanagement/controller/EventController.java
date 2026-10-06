package com.events.eventmanagement.controller;

import com.events.eventmanagement.dto.EventRequest;
import com.events.eventmanagement.dto.EventResponse;
import com.events.eventmanagement.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ---------- Public browsing ----------

    @GetMapping("/api/events")
    public List<EventResponse> browseEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        return eventService.searchApprovedEvents(search, category);
    }

    @GetMapping("/api/events/{id}")
    public EventResponse getEvent(@PathVariable Long id) {
        return eventService.getPublicEvent(id);
    }

    // ---------- Organizer ----------

    @GetMapping("/api/organizers/events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<EventResponse> myEvents() {
        return eventService.listMyEvents();
    }

    @PostMapping("/api/organizers/events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    @GetMapping("/api/organizers/events/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse getMyEvent(@PathVariable Long id) {
        return eventService.getMyEvent(id);
    }

    @PutMapping("/api/organizers/events/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse updateMyEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return eventService.updateMyEvent(id, request);
    }

    @DeleteMapping("/api/organizers/events/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteMyEvent(@PathVariable Long id) {
        eventService.deleteMyEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Admin: event approval ----------

    @GetMapping("/api/admin/events")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EventResponse> allEvents() {
        return eventService.listAllEvents();
    }

    @PostMapping("/api/admin/events/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse approve(@PathVariable Long id) {
        return eventService.approveEvent(id);
    }

    @PostMapping("/api/admin/events/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return eventService.rejectEvent(id, reason);
    }
}
