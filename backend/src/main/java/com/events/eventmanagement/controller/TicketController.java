package com.events.eventmanagement.controller;

import com.events.eventmanagement.dto.TicketTypeRequest;
import com.events.eventmanagement.dto.TicketTypeResponse;
import com.events.eventmanagement.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/api/events/{eventId}/tickets")
    public List<TicketTypeResponse> publicTickets(@PathVariable Long eventId) {
        return ticketService.listPublicTickets(eventId);
    }

    @PostMapping("/api/organizers/events/{eventId}/tickets")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<TicketTypeResponse> createTicket(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.createTicket(eventId, request));
    }

    @PutMapping("/api/organizers/tickets/{ticketId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public TicketTypeResponse updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketTypeRequest request) {
        return ticketService.updateTicket(ticketId, request);
    }

    @DeleteMapping("/api/organizers/tickets/{ticketId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }
}
