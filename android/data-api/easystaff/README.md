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

## Reverse Engineering

The API was reverse engineered by analyzing the underlying JSON endpoints used by the Agenda Web frontend. Most operations use undocumented JSON APIs:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/combo.php` | GET | Returns JavaScript with JSON data for dropdowns (academic years, programs, teachers, buildings, rooms) |
| `/grid_call.php` | POST | Returns JSON schedule grid data |
| `/test_call.php` | POST | Returns JSON exam calendar data |
| `/rooms_call.php` | POST | Returns JSON room occupation data |
| `/bookings_call.php` | POST | Returns JSON event search results |

The `combo.php` endpoint returns data embedded in JavaScript variable assignments (e.g., `var elenco_corsi = [...];`), which the client extracts and parses as JSON.

Only one operation uses HTML scraping: `buildings.getRoomDetails()` parses the Vetrina Aule HTML page for detailed room information (capacity, equipment, accessibility).

## Dependencies

- Ktor 3.3 (HTTP client)
- Kotlinx Serialization (JSON)
- Jsoup 1.22 (HTML parsing for room details only)
