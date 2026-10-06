package com.events.eventmanagement.repository;

import com.events.eventmanagement.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEventIdAndActiveTrue(Long eventId);
    List<TicketType> findByEventId(Long eventId);
}
