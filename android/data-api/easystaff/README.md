# EasyStaff API Module

Kotlin API client for the [Agenda Web](https://gestioneorari.didattica.unimib.it/PortaleStudentiUnimib/) scheduling platform.

## Overview

This module provides a type-safe Kotlin client for interacting with the Agenda Web platform, powered by [EasyStaff](https://www.easystaff.it/). It's the official scheduling system for the University of Milano-Bicocca, providing lesson timetables, exam calendars, and room information.

## Key Features

- **No authentication required** - All schedule data is publicly accessible
- **Multi-language support** - Italian, English, Spanish, German, French
- **Multiple search modes** - Search by program, teacher, or subject

## API Coverage

| Sub-API      | Description                                        |
|--------------|----------------------------------------------------|
| `core`       | Academic years, teaching areas, study programs     |
| `schedule`   | Lesson timetables (Orario delle lezioni)           |
| `exams`      | Exam calendar (Calendario esami)                   |
| `buildings`  | Room occupation and showcase (Aule)                |
| `events`     | Universal event search (Ricerca eventi)            |
| `attendance` | Attendance tracking (Obbligo di Presenza)          |
| `planning`   | Appointment booking (Portale Planning)             |

## Usage

```kotlin
val api = EasyStaffApi()

// Get available academic years
val years = api.core.getAcademicYears()

// Get lesson schedule for a study program
val schedule = api.schedule.getScheduleByProgram(
    academicYear = academicYear,
    studyProgram = studyProgram,
    yearsOfStudy = yearsOfStudy,
    weekStartDate = LocalDate.now()
)

// Get exam calendar
val exams = api.exams.getExamsByProgram(
    studyProgram = studyProgram,
    yearsOfStudy = yearsOfStudy,
    startDate = LocalDate.now(),
    endDate = LocalDate.now().plusMonths(3)
)

// Get room occupation for a building
val occupation = api.buildings.getBuildingOccupation(
    building = building,
    date = LocalDate.now()
)

// Search events
val events = api.events.getEvents(
    startDate = LocalDate.now(),
    endDate = LocalDate.now().plusDays(7),
    buildings = listOf(building)
)

// Don't forget to close when done
api.close()
```

## Portale Planning (appointment booking)

The `planning` sub-API targets a separate backend on the same host (`/portaleplanningAPI/api`),
which powers the university booking portals. Every request carries the numeric portal id in the
`X-CLIENTE` header; each portal is exposed as a portal-bound sub-API:

- `planning.informationDesks` - "Sportelli Informativi" (`unimib-segreteria`, id 2),
  appointments with the student administration offices. Anonymous booking is fully supported
  (`EasyStaffPlanningBookingApi`).
- `planning.studyRooms` - the legacy study rooms portal (`unimib-aulestudio`, id 1). Public
  login is mandatory and currently blocked, so only configuration and discovery are exposed
  (`EasyStaffPlanningPortalApi`) and listings return empty for anonymous users (study room
  booking has moved to Affluences). The university SAML bridge for this backend is also
  broken: it mints a JWT with all-null claims without contacting the IdP, so authenticated
  endpoints are not modeled.

Reservations follow a provisional-hold flow: `createReservation` holds the slot, and the hold
must be finalized with `confirmReservation` within the booking session window (the web
front-end uses 30 minutes) or discarded with `deleteReservation(force = true)`. Reservations
are keyed by a reservation code plus the value of the primary form field (the email address),
which together authorize managing, editing, and cancelling.

```kotlin
val desks = api.planning.informationDesks

// Pick a service and an area offering it (filtering by service also embeds
// the full booking constraints of each area's services)
val service = desks.getServices().first()
val area = desks.getAreas(service.id).first()

// Find a day and a slot
val monthSchedule = desks.getMonthSchedule(service.id, area.id, YearMonth.now(), service.durationSeconds)
val day = monthSchedule.days.keys.first()
val daySchedule = desks.getDaySchedule(service.id, area.id, day, service.durationSeconds)
val slot = daySchedule.slots.entries.first { it.value.availableCount > 0 }

// Fill the dynamic booking form and create a provisional hold
val form = desks.getServiceForm(service.id)
val request = desks.buildReservationRequest(
    serviceId = service.id,
    areaId = area.id,
    slotStart = LocalDateTime.of(day, slot.key.start),
    durationSeconds = service.durationSeconds,
    formFields = form,
    values = mapOf(
        "email" to "student@campus.unimib.it",
        "cognome_nome" to "ROSSI MARIO",
        "note_aggiuntive" to "Richiesta informazioni"
    )
)
val reservation = desks.createReservation(request)

// Finalize the hold
desks.confirmReservation(reservation.entryId!!)
```

## Reverse Engineering

The API was reverse engineered by analyzing the underlying JSON endpoints used by the Agenda Web frontend. Most operations use undocumented JSON APIs:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/combo.php` | GET | Returns JavaScript with JSON data for dropdowns (academic years, programs, teachers, buildings, rooms) |
| `/grid_call.php` | POST | Returns JSON schedule grid data |
| `/test_call.php` | POST | Returns JSON exam calendar data |
| `/rooms_call.php` | POST | Returns JSON room occupation data |
| `/bookings_call.php` | POST | Returns JSON event search results |
| `/portaleplanningAPI/api/*` | GET/POST | JSON booking backend of the Portale Planning portals (services, areas, availability schedules, reservation lifecycle) |

The `combo.php` endpoint returns data embedded in JavaScript variable assignments (e.g., `var elenco_corsi = [...];`), which the client extracts and parses as JSON.

Only one operation uses HTML scraping: `buildings.getRoomDetails()` parses the Vetrina Aule HTML page for detailed room information (capacity, equipment, accessibility).