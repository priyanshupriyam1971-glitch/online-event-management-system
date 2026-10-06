package com.events.eventmanagement.controller;

import com.events.eventmanagement.dto.MessageRequest;
import com.events.eventmanagement.dto.MessageResponse;
import com.events.eventmanagement.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/api/organizers/events/{eventId}/messages")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long eventId,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendToAttendees(eventId, request));
    }

    @GetMapping("/api/organizers/events/{eventId}/messages")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<MessageResponse> eventMessages(@PathVariable Long eventId) {
        return messageService.listEventMessages(eventId);
    }

    @GetMapping("/api/attendees/notifications")
    public List<MessageResponse> myNotifications() {
        return messageService.listMyNotifications();
    }
}
