package com.events.eventmanagement.service;

import com.events.eventmanagement.dto.EventRequest;
import com.events.eventmanagement.dto.EventResponse;
import com.events.eventmanagement.entity.Event;
import com.events.eventmanagement.entity.TicketType;
import com.events.eventmanagement.entity.User;
import com.events.eventmanagement.enums.EventStatus;
import com.events.eventmanagement.exception.BadRequestException;
import com.events.eventmanagement.exception.ResourceNotFoundException;
import com.events.eventmanagement.repository.ActivityLogRepository;
import com.events.eventmanagement.repository.EventRepository;
import com.events.eventmanagement.repository.TicketTypeRepository;
import com.events.eventmanagement.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AuthService authService;

    public EventService(EventRepository eventRepository,
                        TicketTypeRepository ticketTypeRepository,
                        UserRepository userRepository,
                        ActivityLogRepository activityLogRepository,
                        AuthService authService) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.authService = authService;
    }

    // ---------- Public browsing ----------

    public List<EventResponse> searchApprovedEvents(String search, String category) {
        List<Event> events = eventRepository.findAll(approvedSpec(search, category));
        return events.stream()
                .map(e -> EventResponse.from(e, ticketTypeRepository.findByEventIdAndActiveTrue(e.getId())))
                .toList();
    }

    public EventResponse getPublicEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getStatus() != EventStatus.APPROVED) {
            throw new BadRequestException("This event is not published yet");
        }
        return EventResponse.from(event, ticketTypeRepository.findByEventIdAndActiveTrue(id));
    }

    // ---------- Organizer: my events ----------

    public List<EventResponse> listMyEvents() {
        User organizer = authService.loadCurrentUser();
        return eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizer.getId()).stream()
                .map(e -> EventResponse.from(e, ticketTypeRepository.findByEventId(e.getId())))
                .toList();
    }

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        User organizer = authService.loadCurrentUser();
        if (organizer.getRole().name().equals("ATTENDEE")) {
            throw new BadRequestException("Only organizers can create events");
        }

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setCategory(request.category());
        event.setVenue(request.venue());
        event.setEventDate(request.eventDate());
        event.setStatus(EventStatus.PENDING); // awaits admin approval
        eventRepository.save(event);

        logActivity(organizer.getName(), "EVENT_CREATED",
                organizer.getName() + " submitted event: " + event.getTitle());

        return EventResponse.from(event, List.of());
    }

    public EventResponse getMyEvent(Long id) {
        return EventResponse.from(loadOwnedEvent(id), ticketTypeRepository.findByEventId(id));
    }

    /** Verify the current organizer owns this event (admin always passes) and return the entity. */
    public Event verifyOrganizerOwnsEvent(Long id) {
        return loadOwnedEvent(id);
    }

    @Transactional
    public EventResponse updateMyEvent(Long id, EventRequest request) {
        Event event = loadOwnedEvent(id);
        if (event.getStatus() == EventStatus.APPROVED) {
            throw new BadRequestException("Approved events cannot be edited; contact the administrator");
        }
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setCategory(request.category());
        event.setVenue(request.venue());
        event.setEventDate(request.eventDate());
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
        return EventResponse.from(event, ticketTypeRepository.findByEventId(id));
    }

    @Transactional
    public void deleteMyEvent(Long id) {
        Event event = loadOwnedEvent(id);
        eventRepository.delete(event);
        logActivity(authService.loadCurrentUser().getName(), "EVENT_DELETED", "Event deleted: " + event.getTitle());
    }

    // ---------- Admin: approval ----------

    public List<EventResponse> listAllEvents() {
        return eventRepository.findAll().stream()
                .map(e -> EventResponse.from(e, ticketTypeRepository.findByEventId(e.getId())))
                .toList();
    }

    @Transactional
    public EventResponse approveEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.setStatus(EventStatus.APPROVED);
        event.setRejectionReason(null);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);

        logActivity("ADMIN", "EVENT_APPROVED", "Event approved: " + event.getTitle());
        return EventResponse.from(event, ticketTypeRepository.findByEventId(id));
    }

    @Transactional
    public EventResponse rejectEvent(Long id, String reason) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.setStatus(EventStatus.REJECTED);
        event.setRejectionReason(reason != null ? reason : "Rejected by administrator");
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);

        logActivity("ADMIN", "EVENT_REJECTED", "Event rejected: " + event.getTitle());
        return EventResponse.from(event, ticketTypeRepository.findByEventId(id));
    }

    // ---------- Internal ----------

    private Event loadOwnedEvent(Long id) {
        User organizer = authService.loadCurrentUser();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!event.getOrganizer().getId().equals(organizer.getId()) && organizer.getRole().name().equals("ORGANIZER")) {
            throw new BadRequestException("You can only manage your own events");
        }
        return event;
    }

    private Specification<Event> approvedSpec(String search, String category) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), EventStatus.APPROVED));

            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("venue")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            query.orderBy(cb.asc(root.get("eventDate")));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private void logActivity(String actor, String action, String details) {
        activityLogRepository.save(new com.events.eventmanagement.entity.ActivityLog(
                actor, action, details, LocalDateTime.now()));
    }
}
