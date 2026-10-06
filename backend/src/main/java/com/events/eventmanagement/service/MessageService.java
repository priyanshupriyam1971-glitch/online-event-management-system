package com.events.eventmanagement.service;

import com.events.eventmanagement.dto.MessageRequest;
import com.events.eventmanagement.dto.MessageResponse;
import com.events.eventmanagement.entity.Notification;
import com.events.eventmanagement.repository.NotificationRepository;
import com.events.eventmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final AuthService authService;

    public MessageService(NotificationRepository notificationRepository,
                          UserRepository userRepository,
                          EventService eventService,
                          AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.authService = authService;
    }

    @Transactional
    public MessageResponse sendToAttendees(Long eventId, MessageRequest request) {
        var organizer = authService.loadCurrentUser();
        var event = eventService.verifyOrganizerOwnsEvent(eventId);

        Notification notification = new Notification();
        notification.setEvent(event);
        notification.setOrganizer(organizer);
        notification.setTitle(request.title());
        notification.setContent(request.content());
        notificationRepository.save(notification);

        return MessageResponse.from(notification);
    }

    public List<MessageResponse> listEventMessages(Long eventId) {
        eventService.verifyOrganizerOwnsEvent(eventId);
        return notificationRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    public List<MessageResponse> listMyNotifications() {
        var attendee = authService.loadCurrentUser();
        return notificationRepository.findNotificationsForAttendee(attendee.getId()).stream()
                .map(MessageResponse::from)
                .toList();
    }
}
