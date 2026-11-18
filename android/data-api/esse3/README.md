# Esse3 API Module

Kotlin API client for [Segreterie OnLine](https://s3w.si.unimib.it/) (Esse3), powered by [CINECA](https://www.cineca.it/).

> **IMPORTANT**: This module was created after discovering an undocumented ("secret") Esse3 REST API.
> Because of that discovery, we only read web pages for what it doesn't cover.

## Overview

This module provides a typed Kotlin client for the Esse3 REST platform under `https://s3w.si.unimib.it/e3rest/api`,
generated from `common/specs/esse3`. It works with structured JSON endpoints, except for the
web portal pages the REST API doesn't cover, which are read out of their HTML as modules of the
same `Esse3Api` (`common/specs/esse3/web`):

| Module         | What it is                                                                          |
|----------------|-------------------------------------------------------------------------------------|
| `certificates` | The self-declarations (autocertificazioni) a student can print, and their PDFs     |

The portal pages need the session cookies of the university's SSO login, not the REST API's
Basic credentials, so they are called on a client of their own carrying those cookies. When the
session expires, the pages report it (`Esse3CertificatesPage.sessionExpired`).

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

Every module extends the generated runtime's `RestApi`, with the policy `common/specs/esse3/client.yaml` describes:

- Base URL: `https://s3w.si.unimib.it/e3rest/api`
- List services answering HTTP 404 return an empty list
- Failures become an `Esse3Exception` carrying Esse3's `retCode` and `retErrMsg`, and the roles the operation requires

Each domain API maps to a service base path (examples):

| API class                       | Service path                          |
|---------------------------------|---------------------------------------|
| `Esse3AuthApi`                  | `/`                                   |
| `Esse3CareersApi`               | `/carriere-service-v1`                |
| `Esse3TranscriptApi`            | `/libretto-service-v2`                |
| `Esse3TuitionFeesApi`           | `/tasse-service-v1`                   |
| `Esse3PersonalDataApi`          | `/anagrafica-service-v1`              |
| `Esse3QuestionnairesApi`        | `/questionari-service-v1`             |
| `Esse3PlansApi`                 | `/piani-service-v1`                   |
| `Esse3RecordsApi`               | `/verbali-service-v1`                 |
| `Esse3CommunicationsApi`       | `/comunicazioni-service-v1`           |
| `Esse3CompetitionsApi`          | `/concorsi-service-v2`                |
| `Esse3AttachmentsApi`           | `/allegati-service-v1`                |

### Code generation

API classes and DTOs are generated from the Esse3 specs in [common/specs/esse3](../../../common/specs/esse3) by the shared code generator in [common/codegen](../../../common/codegen), which also produces the Swift and web SDKs. Every file is generated, the client, `Esse3Exception` and the `Esse3PermissionLevel` roles included, and must not be edited.
