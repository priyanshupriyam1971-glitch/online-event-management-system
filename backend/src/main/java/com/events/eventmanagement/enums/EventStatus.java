package com.events.eventmanagement.enums;

public enum EventStatus {
    PENDING,    // submitted by organizer, waiting for admin review
    APPROVED,   // published and visible to attendees
    REJECTED    // rejected by admin
}
