package com.events.eventmanagement.repository;

import com.events.eventmanagement.entity.Event;
import com.events.eventmanagement.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);
    List<Event> findByStatus(EventStatus status);
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);
    long countByStatus(EventStatus status);
}
