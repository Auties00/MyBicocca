# Esse3 API Module

Kotlin API client for [Segreterie OnLine](https://s3w.si.unimib.it/) (Esse3), powered by [CINECA](https://www.cineca.it/).

> **IMPORTANT**: This module was created after discovering an undocumented ("secret") Esse3 REST API.
> Because of that discovery, we no longer needed HTML scraping for core features.
> The legacy scraping implementation is kept in `../esse3-scraper`.

## Overview

This module provides a typed Kotlin client for the Esse3 REST platform under `https://s3w.si.unimib.it/e3rest/api`.
Unlike `esse3-scraper`, it works with structured JSON endpoints instead of parsing server-rendered HTML pages.

## Authentication

Authentication is typically performed with Basic credentials plus the Esse3 session login endpoint:

1. Configure `Authorization: Basic ...` in the client
2. Call `api.auth.login()`
3. Use the returned session data/tokens and existing cookies for subsequent calls

```kotlin
import io.ktor.client.request.defaultRequest
import java.util.Base64

val api = Esse3Api {
    defaultRequest {
        val credentials = Base64.getEncoder()
            .encodeToString("$username:$password".toByteArray())
        header("Authorization", "Basic $credentials")
    }
}

val session = api.auth.login()
println("Logged in user: ${session.user.userId}")

val careers = api.careers.getCareers(userId = session.user.userId)
println("Careers found: ${careers.size}")

val enrollmentId = careers.firstOrNull()?.enrollmentId?.toLong()
if (enrollmentId != null) {
    val transcriptRows = api.transcript.getRecordBookRows(enrollmentId)
    println("Transcript rows: ${transcriptRows.size}")
}

api.close()
```

## API Coverage

| Sub-API                  | Description                                    |
|--------------------------|------------------------------------------------|
| `attachments`            | Document attachments and uploads               |
| `auth`                   | Login, session, JWT operations                 |
| `logging`                | System logging                                 |
| `personalData`           | Student profile, addresses, bank details       |
| `careers`                | Careers, enrollments, student status           |
| `teachers`               | Teacher information and authorizations         |
| `badgeImport`            | Badge import operations                        |
| `countries`              | Geographic data, country listings              |
| `internships`            | Internship applications, companies, projects   |
| `questionnaires`         | Surveys, evaluations, questionnaire answers    |
| `competitions`           | Scholarship and competition management         |
| `tuitionFees`            | Invoices, payments, exemptions, scholarships   |
| `structure`              | University organizational structure            |
| `choiceRules`            | Activity choice rules                          |
| `logistics`              | Logistics and resource management              |
| `offer`                  | Course offerings                               |
| `ruleProperties`         | Rule properties                                |
| `teacherReporting`       | Teacher reporting                              |
| `calesa`                 | Academic calendar events                       |
| `careerUpdate`           | Career modifications                           |
| `transcript`             | Transcript rows, exam attempts, averages       |
| `plans`                  | Study plan retrieval and updates               |
| `records`                | Exam reports and outcomes                      |
| `degreeAward`            | Degree conferment                              |
| `communications`         | Messages and notifications                     |
| `badge`                  | Badge classes and student badges               |
| `services`               | Student service endpoints                      |
| `users`                  | User management                                |
| `appointmentsCalendar`   | Appointment scheduling                         |

## Implementation Details

### REST service architecture

All clients extend `Esse3AbstractApi`, which defines:

- Base URL: `https://s3w.si.unimib.it/e3rest/api`
- Common HTTP helpers for GET/POST/PUT/PATCH/DELETE
- Shared permission-aware response validation

Each domain API maps to a service base path (examples):

| API class                       | Service path                          |
|---------------------------------|---------------------------------------|
| `Esse3AuthApi`                  | `/`                                   |
| `Esse3CareersApi`               | `/carriere-service-v1`                |
| `Esse3TranscriptApi`            | `/libretto-service-v2`                |
| `Esse3TuitionFeesApi`           | `/tasse-service-v1`                   |
| `Esse3PersonalDataApi`          | `/anagrafica-service-v1`              |
| `Esse3QuestionnairesApi`        | `/questionari-service-v1`             |
| `Esse3InternshipsApi`           | `/tirocini-service-v1`                |
| `Esse3PlansApi`                 | `/piani-service-v1`                   |
| `Esse3RecordsApi`               | `/verbali-service-v1`                 |
| `Esse3CommunicationsApi`       | `/comunicazioni-service-v1`           |
| `Esse3CompetitionsApi`          | `/concorsi-service-v2`                |
| `Esse3AttachmentsApi`           | `/allegati-service-v1`                |

### Code generation

API classes and DTOs are auto-generated from OpenAPI specs using a custom Kotlin code generator in [codegen](./codegen).
