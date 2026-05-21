# MyBicocca Data API

Kotlin libraries for accessing University of Milano-Bicocca digital services.

## Overview

This module provides type-safe API clients for integrating with the university's platforms. Each service is wrapped in a clean, coroutine-based API with proper error handling and serialization.

### Modules

| Module      | Platform                                                                       | Description                                                |
|-------------|--------------------------------------------------------------------------------|------------------------------------------------------------|
| `esse3`     | [Segreterie OnLine](https://s3w.si.unimib.it)                                  | Student administration (grades, exams, taxes, internships) |
| `elearning` | [Moodle](https://elearning.unimib.it)                                          | E-learning platform (courses, assignments, quizzes)        |
| `easystaff` | [Agenda Web](https://gestioneorari.didattica.unimib.it)                        | Scheduling system (lessons, exams, rooms)                  |
| `bicoccapp` | [BicoccApp](https://play.google.com/store/apps/details?id=it.bicoccapp.unimib) | Official app backend API                                   |

## Tech Stack

- **Kotlin** 2.3
- **Ktor** 3.3 - HTTP client
- **Ktorfit** 2.7 - Type-safe HTTP client generator
- **Kotlinx Serialization** - JSON parsing
- **Jsoup** 1.22 - HTML parsing
- **Coroutines** 1.10 - Async operations
- **JUnit 5** - Testing

## Requirements

- JDK 17+
- Gradle 8.x

## Project Structure

All Kotlin code lives under the base package `it.attendance100.mybicocca.data.remote.*`. Each module owns one subpackage; shared utilities live in `data.remote.common.*`.

```
data-api/
├── build.gradle.kts       # Root configuration
├── src/main/kotlin/.../data/remote/common/   # shared utilities (util/, exception/)
├── esse3/                 # data.remote.esse3.{api,dto,exception}
├── elearning/             # data.remote.elearning.{api,dto,exception}
├── easystaff/             # data.remote.easystaff.{api,dto}
└── bicoccapp/             # data.remote.bicoccapp.{api,dto}
```

Per-module layout (e.g. `esse3/`):
```
esse3/src/main/kotlin/it/attendance100/mybicocca/data/remote/esse3/
├── api/         # Ktor/Ktorfit clients (Esse3Api, Esse3CareerApi, ...)
├── dto/         # @Serializable data classes
└── exception/   # API-specific exceptions
```

## Usage

### EasyStaff (No Authentication)

```kotlin
val api = EasyStaffApi()

// Lesson schedules
val schedule = api.schedule.getSchedule(academicYear = "2024", programCode = "E3001Q")

// Exam calendar
val exams = api.exams.getExamCalendar(academicYear = "2024", programCode = "E3001Q")

// Room availability
val rooms = api.buildings.getRoomOccupation(buildingCode = "U6", date = LocalDate.now())

api.close()
```

### E-Learning (Moodle Token)

```kotlin
val api = ElearningApi()

val courses = api.courses.getEnrolledCourses(token, userId)
val assignments = api.assignments.getAssignments(token, courseId)
val grades = api.grades.getCourseGrades(token, courseId, userId)

api.close()
```

### Esse3 (SSO Authentication)

```kotlin
// Authenticate via Shibboleth
val cookies = Esse3AuthApi(httpClient).authenticate(username, password)

val api = Esse3Api(sessionCookies = cookies)

val homepage = api.profile.getHomepage()
val libretto = api.career.getLibretto()
val taxes = api.taxes.getBillList()

api.close()
```

## API Reference

### Esse3Api
| Sub-API          | Description                       |
|------------------|-----------------------------------|
| `auth`           | Shibboleth SSO authentication     |
| `profile`        | Student profile, homepage, photo  |
| `career`         | Libretto, study plan, evaluations |
| `exams`          | Sessions, reservations, results   |
| `taxes`          | Bills, payments, receipts         |
| `internships`    | Search, applications, companies   |
| `questionnaires` | Course evaluations, surveys       |

### ElearningApi
| Sub-API       | Description                 |
|---------------|-----------------------------|
| `site`        | Site info, token validation |
| `users`       | User profiles               |
| `courses`     | Enrolled courses, content   |
| `quizzes`     | Quiz attempts, results      |
| `assignments` | Submissions, feedback       |
| `forums`      | Discussions, posts          |
| `calendar`    | Events, deadlines           |
| `badges`      | Earned badges               |
| `completion`  | Progress tracking           |
| `grades`      | Grade reports               |
| `messages`    | Private messaging           |

### EasyStaffApi
| Sub-API      | Description                        |
|--------------|------------------------------------|
| `core`       | Academic years, programs, teachers |
| `schedule`   | Lesson timetables                  |
| `exams`      | Exam calendar                      |
| `buildings`  | Rooms, occupation, showcase        |
| `events`     | Event search                       |
| `attendance` | Attendance tracking                |

### BicoccappApi
| Sub-API    | Description       |
|------------|-------------------|
| `auth`     | OAuth2/OIDC flows |
| `profile`  | Student profile   |
| `career`   | Academic progress |
| `exams`    | Exam sessions     |
| `taxes`    | Fee status        |
| `calendar` | Course schedule   |
| `wizard`   | Course selection  |
| `campus`   | POIs, maps        |

## License

For educational and personal use only.
