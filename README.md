# Online Event Management System

A web-based platform for the complete event lifecycle: create → approve → publish → register → ticket → communicate → attend.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17+, Spring Boot 3.5, Spring Security + JWT, Spring Data JPA |
| Database | MySQL (H2 in-memory for zero-config dev) |
| Frontend | React 19 + Vite, Axios |
| Build | Maven (backend), npm (frontend) |

## Quick Start (H2 in-memory database — no setup needed)

**Terminal 1 — Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Terminal 2 — Frontend:**
```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## Using MySQL instead

1. Create the database: `CREATE DATABASE event_management;`
2. Update `backend/src/main/resources/application-mysql.properties` (username/password).
3. Run with the MySQL profile:
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Demo Accounts (auto-seeded on startup)

| Role | Email | Password |
|---|---|---|
| Admin | admin@event.com | admin123 |
| Organizer | organizer@event.com | organizer123 |
| Attendee | attendee@event.com | attendee123 |

## Roles

- **Admin** — manage users/roles, approve or reject events, view platform stats, monitor activity
- **Organizer** — create events, manage ticket types (price/quantity), message attendees, view sales
- **Attendee** — browse/search events, register, buy tickets, track registrations, receive updates

## API Overview

```
POST   /api/auth/register               register (ATTENDEE or ORGANIZER)
POST   /api/auth/login                  login → JWT
GET    /api/auth/me                     current user

GET    /api/events                      public event browsing (search, category)
GET    /api/events/{id}                 public event details + tickets
GET    /api/events/{id}/tickets         public ticket listing
POST   /api/events/{id}/register        attendee registration + ticket purchase

GET    /api/organizers/events           my events (all statuses)
POST   /api/organizers/events           create event (goes to PENDING)
PUT    /api/organizers/events/{id}      update own event
DELETE /api/organizers/events/{id}      delete own event
GET    /api/organizers/events/{id}/registrations   attendees of my event
POST   /api/organizers/events/{id}/tickets         add ticket type
PUT    /api/organizers/tickets/{id}                update ticket type
DELETE /api/organizers/tickets/{id}                delete ticket type
POST   /api/organizers/events/{id}/messages        message attendees
GET    /api/organizers/stats             organizer dashboard stats

GET    /api/attendees/registrations    my registrations
GET    /api/attendees/notifications    updates from organizers

GET    /api/admin/events               all events (with status)
POST   /api/admin/events/{id}/approve  approve event
POST   /api/admin/events/{id}/reject   reject event
GET    /api/admin/users                user management
PUT    /api/admin/users/{id}           update user (name/role/enabled)
DELETE /api/admin/users/{id}           delete user
GET    /api/admin/stats                platform statistics
GET    /api/admin/activities           activity log
```

## Notes

- Ticket payment is recorded with a `PAID` (mock) status — a real payment gateway is future scope.
- `spring.jpa.hibernate.ddl-auto=update` is for development; use Flyway/Liquibase migrations for production.
- JWT secret in `application.properties` must be replaced for production use.
