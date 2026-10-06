package com.events.eventmanagement.service;

import com.events.eventmanagement.dto.RegistrationRequest;
import com.events.eventmanagement.dto.RegistrationResponse;
import com.events.eventmanagement.dto.TicketItemRequest;
import com.events.eventmanagement.entity.Event;
import com.events.eventmanagement.entity.Registration;
import com.events.eventmanagement.entity.TicketItem;
import com.events.eventmanagement.entity.TicketType;
import com.events.eventmanagement.enums.EventStatus;
import com.events.eventmanagement.enums.PaymentStatus;
import com.events.eventmanagement.exception.BadRequestException;
import com.events.eventmanagement.exception.ResourceNotFoundException;
import com.events.eventmanagement.repository.ActivityLogRepository;
import com.events.eventmanagement.repository.EventRepository;
import com.events.eventmanagement.repository.RegistrationRepository;
import com.events.eventmanagement.repository.TicketItemRepository;
import com.events.eventmanagement.repository.TicketTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketItemRepository ticketItemRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AuthService authService;
    private final EventService eventService;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               TicketTypeRepository ticketTypeRepository,
                               TicketItemRepository ticketItemRepository,
                               ActivityLogRepository activityLogRepository,
                               AuthService authService,
                               EventService eventService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketItemRepository = ticketItemRepository;
        this.activityLogRepository = activityLogRepository;
        this.authService = authService;
        this.eventService = eventService;
    }

    @Transactional
    public RegistrationResponse register(Long eventId, RegistrationRequest request) {
        var attendee = authService.loadCurrentUser();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getStatus() != EventStatus.APPROVED) {
            throw new BadRequestException("This event is not open for registration");
        }

        Registration registration = new Registration();
        registration.setAttendee(attendee);
        registration.setEvent(event);
        registration.setRegistrationDate(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (TicketItemRequest itemRequest : request.items()) {
            TicketType ticketType = ticketTypeRepository.findById(itemRequest.ticketTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

            if (!ticketType.getEvent().getId().equals(eventId)) {
                throw new BadRequestException("Ticket type does not belong to this event");
            }
            if (!ticketType.isActive()) {
                throw new BadRequestException("Ticket type '" + ticketType.getName() + "' is no longer available");
            }
            if (itemRequest.quantity() <= 0) {
                throw new BadRequestException("Ticket quantity must be positive");
            }
            int remaining = ticketType.getQuantity() - ticketType.getSoldQuantity();
            if (itemRequest.quantity() > remaining) {
                throw new BadRequestException("Only " + remaining + " ticket(s) left for '" + ticketType.getName() + "'");
            }

            ticketType.setSoldQuantity(ticketType.getSoldQuantity() + itemRequest.quantity());
            ticketTypeRepository.save(ticketType);

            TicketItem item = new TicketItem();
            item.setRegistration(registration);
            item.setTicketType(ticketType);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(ticketType.getPrice());
            item.setTotalAmount(ticketType.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
            item.setPaymentStatus(PaymentStatus.PENDING); // confirmed when admin approves
            registration.getItems().add(item);

            total = total.add(item.getTotalAmount());
        }

        registration.setTotalAmount(total);
        registrationRepository.save(registration);

        activityLogRepository.save(new com.events.eventmanagement.entity.ActivityLog(
                attendee.getName(), "REGISTRATION",
                attendee.getName() + " registered for event: " + event.getTitle()
                        + " (" + total + ")", LocalDateTime.now()));

        return RegistrationResponse.from(registration);
    }

    public List<RegistrationResponse> listMyRegistrations() {
        var attendee = authService.loadCurrentUser();
        return registrationRepository.findByAttendeeIdOrderByRegistrationDateDesc(attendee.getId()).stream()
                .map(RegistrationResponse::from)
                .toList();
    }

    public List<RegistrationResponse> listEventRegistrations(Long eventId) {
        eventService.verifyOrganizerOwnsEvent(eventId);
        return registrationRepository.findByEventIdOrderByRegistrationDateDesc(eventId).stream()
                .map(RegistrationResponse::from)
                .toList();
    }
}
