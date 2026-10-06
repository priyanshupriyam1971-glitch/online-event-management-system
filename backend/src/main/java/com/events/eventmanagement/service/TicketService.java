package com.events.eventmanagement.service;

import com.events.eventmanagement.dto.TicketTypeRequest;
import com.events.eventmanagement.dto.TicketTypeResponse;
import com.events.eventmanagement.entity.Event;
import com.events.eventmanagement.entity.TicketType;
import com.events.eventmanagement.exception.BadRequestException;
import com.events.eventmanagement.exception.ResourceNotFoundException;
import com.events.eventmanagement.repository.EventRepository;
import com.events.eventmanagement.repository.TicketTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TicketService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    public TicketService(TicketTypeRepository ticketTypeRepository,
                         EventRepository eventRepository,
                         EventService eventService) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }

    public List<TicketTypeResponse> listPublicTickets(Long eventId) {
        return ticketTypeRepository.findByEventIdAndActiveTrue(eventId).stream()
                .map(TicketTypeResponse::from)
                .toList();
    }

    @Transactional
    public TicketTypeResponse createTicket(Long eventId, TicketTypeRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        // ensures the current user owns this event
        eventService.getMyEvent(eventId);

        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);
        ticketType.setName(request.name());
        ticketType.setPrice(request.price());
        ticketType.setQuantity(request.quantity());
        ticketTypeRepository.save(ticketType);

        return TicketTypeResponse.from(ticketType);
    }

    @Transactional
    public TicketTypeResponse updateTicket(Long ticketTypeId, TicketTypeRequest request) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
        // ensures the current user owns the event this ticket belongs to
        eventService.getMyEvent(ticketType.getEvent().getId());

        ticketType.setName(request.name());
        ticketType.setPrice(request.price());
        if (request.quantity() < ticketType.getSoldQuantity()) {
            throw new BadRequestException("Quantity cannot be less than tickets already sold ("
                    + ticketType.getSoldQuantity() + ")");
        }
        ticketType.setQuantity(request.quantity());
        ticketTypeRepository.save(ticketType);
        return TicketTypeResponse.from(ticketType);
    }

    @Transactional
    public void deleteTicket(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
        eventService.getMyEvent(ticketType.getEvent().getId());
        ticketTypeRepository.delete(ticketType);
    }
}
