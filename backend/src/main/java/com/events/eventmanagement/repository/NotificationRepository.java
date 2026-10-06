package com.events.eventmanagement.repository;

import com.events.eventmanagement.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEventIdOrderByCreatedAtDesc(Long eventId);

    // Notifications for events the attendee has confirmed registrations to
    @Query("SELECT n FROM Notification n WHERE n.event.id IN " +
           "(SELECT r.event.id FROM Registration r WHERE r.attendee.id = :attendeeId AND r.status = 'CONFIRMED') " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsForAttendee(Long attendeeId);
}
