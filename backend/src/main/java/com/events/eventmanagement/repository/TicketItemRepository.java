package com.events.eventmanagement.repository;

import com.events.eventmanagement.entity.TicketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface TicketItemRepository extends JpaRepository<TicketItem, Long> {

    // Only confirmed (paid) ticket items count towards revenue and sales analytics

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM TicketItem t WHERE t.paymentStatus = 'PAID'")
    BigDecimal totalRevenue();

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM TicketItem t WHERE t.paymentStatus = 'PAID'")
    int totalTicketsSold();

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM TicketItem t " +
           "WHERE t.ticketType.event.organizer.id = :organizerId AND t.paymentStatus = 'PAID'")
    BigDecimal revenueByOrganizer(Long organizerId);

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM TicketItem t " +
           "WHERE t.ticketType.event.organizer.id = :organizerId AND t.paymentStatus = 'PAID'")
    int ticketsSoldByOrganizer(Long organizerId);

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM TicketItem t " +
           "WHERE t.registration.event.id = :eventId AND t.paymentStatus = 'PAID'")
    int ticketsSoldForEvent(Long eventId);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM TicketItem t " +
           "WHERE t.ticketType.event.id = :eventId AND t.paymentStatus = 'PAID'")
    BigDecimal revenueForEvent(Long eventId);
}
