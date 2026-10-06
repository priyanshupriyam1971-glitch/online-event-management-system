package com.events.eventmanagement.enums;

public enum RegistrationStatus {
    PENDING,    // awaiting admin confirmation
    CONFIRMED,  // approved and paid
    CANCELLED   // rejected by admin (tickets refunded)
}
