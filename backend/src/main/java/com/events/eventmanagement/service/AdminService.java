package com.events.eventmanagement.service;

import com.events.eventmanagement.dto.ActivityLogResponse;
import com.events.eventmanagement.dto.AdminStatsResponse;
import com.events.eventmanagement.dto.OrganizerStatsResponse;
import com.events.eventmanagement.dto.RegistrationResponse;
import com.events.eventmanagement.dto.UserUpdateRequest;
import com.events.eventmanagement.dto.UserResponse;
import com.events.eventmanagement.entity.ActivityLog;
import com.events.eventmanagement.entity.Registration;
import com.events.eventmanagement.entity.TicketItem;
import com.events.eventmanagement.entity.TicketType;
import com.events.eventmanagement.entity.User;
import com.events.eventmanagement.enums.EventStatus;
import com.events.eventmanagement.enums.PaymentStatus;
import com.events.eventmanagement.enums.RegistrationStatus;
import com.events.eventmanagement.exception.BadRequestException;
import com.events.eventmanagement.exception.ResourceNotFoundException;
import com.events.eventmanagement.repository.ActivityLogRepository;
import com.events.eventmanagement.repository.EventRepository;
import com.events.eventmanagement.repository.RegistrationRepository;
import com.events.eventmanagement.repository.TicketItemRepository;
import com.events.eventmanagement.repository.TicketTypeRepository;
import com.events.eventmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketItemRepository ticketItemRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AuthService authService;

    public AdminService(UserRepository userRepository,
                        EventRepository eventRepository,
                        RegistrationRepository registrationRepository,
                        TicketTypeRepository ticketTypeRepository,
                        TicketItemRepository ticketItemRepository,
                        ActivityLogRepository activityLogRepository,
                        AuthService authService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketItemRepository = ticketItemRepository;
        this.activityLogRepository = activityLogRepository;
        this.authService = authService;
    }

    // ---------- User management ----------

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User current = authService.loadCurrentUser();
        if (id.equals(current.getId()) && request.role() != null && request.role() != current.getRole()) {
            throw new BadRequestException("You cannot change your own role");
        }
        if (id.equals(current.getId()) && Boolean.FALSE.equals(request.enabled())) {
            throw new BadRequestException("You cannot disable your own account");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(request.name());
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (id.equals(authService.loadCurrentUser().getId())) {
            throw new BadRequestException("You cannot delete your own account");
        }
        userRepository.deleteById(id);
    }

    // ---------- Dashboards & analytics ----------

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                eventRepository.count(),
                eventRepository.countByStatus(EventStatus.PENDING),
                eventRepository.countByStatus(EventStatus.APPROVED),
                registrationRepository.count(),
                registrationRepository.countByStatus(RegistrationStatus.PENDING),
                ticketItemRepository.totalTicketsSold(),
                ticketItemRepository.totalRevenue(),
                activityLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                        .map(ActivityLogResponse::from)
                        .toList()
        );
    }

    // ---------- Registration approval ----------

    public List<RegistrationResponse> listRegistrations(String status) {
        List<Registration> registrations;
        if (status != null && !status.isBlank()) {
            registrations = registrationRepository.findAll().stream()
                    .filter(r -> r.getStatus().name().equalsIgnoreCase(status))
                    .toList();
        } else {
            registrations = registrationRepository.findAll().stream()
                    .sorted((a, b) -> b.getRegistrationDate().compareTo(a.getRegistrationDate()))
                    .toList();
        }
        return registrations.stream()
                .map(RegistrationResponse::from)
                .toList();
    }

    @Transactional
    public RegistrationResponse approveRegistration(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new BadRequestException("Only pending registrations can be approved");
        }

        registration.setStatus(RegistrationStatus.CONFIRMED);
        for (TicketItem item : registration.getItems()) {
            item.setPaymentStatus(PaymentStatus.PAID);
        }
        registrationRepository.save(registration);

        activityLogRepository.save(new ActivityLog("ADMIN", "REGISTRATION_APPROVED",
                "Registration approved for " + registration.getAttendee().getName()
                        + " — event: " + registration.getEvent().getTitle()
                        + " ($" + registration.getTotalAmount() + ")", LocalDateTime.now()));

        return RegistrationResponse.from(registration);
    }

    @Transactional
    public RegistrationResponse cancelRegistration(Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new BadRequestException("Only pending registrations can be cancelled");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        for (TicketItem item : registration.getItems()) {
            item.setPaymentStatus(PaymentStatus.FAILED);
            // refund: release the reserved tickets
            TicketType ticketType = item.getTicketType();
            ticketType.setSoldQuantity(Math.max(0, ticketType.getSoldQuantity() - item.getQuantity()));
            ticketTypeRepository.save(ticketType);
        }
        registrationRepository.save(registration);

        activityLogRepository.save(new ActivityLog("ADMIN", "REGISTRATION_CANCELLED",
                "Registration cancelled for " + registration.getAttendee().getName()
                        + " — event: " + registration.getEvent().getTitle()
                        + " ($" + registration.getTotalAmount() + " refunded)", LocalDateTime.now()));

        return RegistrationResponse.from(registration);
    }

    public List<ActivityLogResponse> listActivities() {
        return activityLogRepository.findByOrderByCreatedAtDesc().stream()
                .map(ActivityLogResponse::from)
                .toList();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public OrganizerStatsResponse getOrganizerStats() {
        var organizer = authService.loadCurrentUser();
        long myEvents = eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizer.getId()).size();

        long totalRegistrations = registrationRepository.findAll().stream()
                .filter(r -> r.getEvent().getOrganizer().getId().equals(organizer.getId())
                        && r.getStatus() == RegistrationStatus.CONFIRMED)
                .count();

        return new OrganizerStatsResponse(
                myEvents,
                totalRegistrations,
                ticketItemRepository.ticketsSoldByOrganizer(organizer.getId()),
                ticketItemRepository.revenueByOrganizer(organizer.getId())
        );
    }
}
