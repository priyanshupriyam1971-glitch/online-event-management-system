package com.events.eventmanagement;

import com.events.eventmanagement.entity.Event;
import com.events.eventmanagement.entity.TicketType;
import com.events.eventmanagement.entity.User;
import com.events.eventmanagement.enums.EventStatus;
import com.events.eventmanagement.enums.Role;
import com.events.eventmanagement.repository.EventRepository;
import com.events.eventmanagement.repository.TicketTypeRepository;
import com.events.eventmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds demo accounts and sample events on first startup so the platform
 * can be explored immediately. In production this would be replaced by a
 * proper admin setup flow.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                        EventRepository eventRepository,
                        TicketTypeRepository ticketTypeRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User admin = createIfAbsent("System Admin", "admin@event.com", "admin123", Role.ADMIN);
        User organizer = createIfAbsent("Demo Organizer", "organizer@event.com", "organizer123", Role.ORGANIZER);
        createIfAbsent("Demo Attendee", "attendee@event.com", "attendee123", Role.ATTENDEE);

        if (eventRepository.count() == 0) {
            seedEvent(organizer, "Tech Summit 2026",
                    "A one-day summit on modern web technologies, cloud infrastructure and AI. "
                            + "Industry speakers, hands-on workshops and networking sessions.",
                    "Tech", "Galgotias Auditorium, Greater Noida",
                    LocalDateTime.now().plusDays(30).withHour(10).withMinute(0).withSecond(0).withNano(0),
                    new String[][]{{"VIP", "99.50", "10"}, {"Regular", "49.00", "50"}});

            seedEvent(organizer, "Music Fest Night",
                    "Live bands, food stalls and open-air performances under the stars. "
                            + "An evening of music, food and fun for everyone.",
                    "Music", "City Grounds, Noida",
                    LocalDateTime.now().plusDays(21).withHour(18).withMinute(30).withSecond(0).withNano(0),
                    new String[][]{{"General", "25.00", "200"}, {"VIP Lounge", "75.00", "40"}});

            seedEvent(organizer, "Code Hackathon 2026",
                    "A 48-hour hackathon for student developers. Build innovative solutions, "
                            + "win prizes and get mentored by industry experts.",
                    "Tech", "Innovation Lab, Block B",
                    LocalDateTime.now().plusDays(14).withHour(9).withMinute(0).withSecond(0).withNano(0),
                    new String[][]{{"Participant", "10.00", "150"}});

            seedEvent(organizer, "Sports Day",
                    "Annual athletics meet with track events, team games and awards. "
                            + "Cheer for your house and be part of the spirit!",
                    "Sports", "University Stadium",
                    LocalDateTime.now().plusDays(45).withHour(8).withMinute(0).withSecond(0).withNano(0),
                    new String[][]{{"Standard", "20.00", "300"}, {"Family Pack", "50.00", "60"}});

            seedEvent(organizer, "Photography Workshop",
                    "Learn composition, lighting and editing from a professional photographer. " +
                            "Bring your camera or phone — all levels welcome.",
                    "Workshop", "Media Center, Room 204",
                    LocalDateTime.now().plusDays(10).withHour(16).withMinute(0).withSecond(0).withNano(0),
                    new String[][]{{"Student", "5.00", "80"}, {"Professional", "15.00", "20"}});

            System.out.println("[seed] created 5 demo events with tickets");
        }
    }

    private User createIfAbsent(String name, String email, String password, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            userRepository.save(user);
            System.out.println("[seed] created " + role + " account: " + email + " / " + password);
            return user;
        });
    }

    private void seedEvent(User organizer, String title, String description, String category,
                             String venue, LocalDateTime date, String[][] tickets) {
        Event event = new Event();
        event.setOrganizer(organizer);
        event.setTitle(title);
        event.setDescription(description);
        event.setCategory(category);
        event.setVenue(venue);
        event.setEventDate(date);
        event.setStatus(EventStatus.APPROVED);
        eventRepository.save(event);

        for (String[] ticket : tickets) {
            TicketType ticketType = new TicketType();
            ticketType.setEvent(event);
            ticketType.setName(ticket[0]);
            ticketType.setPrice(new BigDecimal(ticket[1]));
            ticketType.setQuantity(Integer.parseInt(ticket[2]));
            ticketTypeRepository.save(ticketType);
        }
    }
}
