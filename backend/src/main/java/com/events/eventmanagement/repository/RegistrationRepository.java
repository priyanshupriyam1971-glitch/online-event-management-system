package com.events.eventmanagement.repository;

import com.events.eventmanagement.entity.Registration;
import com.events.eventmanagement.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByAttendeeIdOrderByRegistrationDateDesc(Long attendeeId);
    List<Registration> findByEventIdOrderByRegistrationDateDesc(Long eventId);
    long countByEventId(Long eventId);
    long countByStatus(RegistrationStatus status);
}
