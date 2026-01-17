# BicoccApp API Module

Kotlin API client for the [BicoccApp](https://play.google.com/store/apps/details?id=it.bicoccapp.unimib) backend REST API.

## Overview

This module provides a type-safe Kotlin client for interacting with the BicoccApp backend API. BicoccApp is the official mobile application of the University of Milano-Bicocca, and this module enables programmatic access to its features.

## API Coverage

| Sub-API    | Description                                   |
|------------|-----------------------------------------------|
| `auth`     | OAuth2/OpenID Connect authentication flows    |
| `profile`  | User profile and student information          |
| `career`   | Academic career, grades, and statistics       |
| `exams`    | Exam sessions and registration                |
| `taxes`    | Tuition fees and payment status               |
| `calendar` | Course schedule and academic calendar         |
| `wizard`   | Course selection wizard                       |
| `campus`   | Campus map, POIs, and teacher directory       |

## Reverse Engineering Documentation

The BicoccApp API was reverse engineered through security research. 
Full documentation of the methodology, tools, and findings is available in the [`documentation/`](./documentation) directory.

## Usage

```kotlin
val bicoccapp = BicoccappApi(
    auth = BicoccappAuthApi(...),
    profile = BicoccappProfileApi(...),
    career = BicoccappCareerApi(...),
    exams = BicoccappExamsApi(...),
    taxes = BicoccappTaxesApi(...),
    calendar = BicoccappCalendarApi(...),
    wizard = BicoccappWizardApi(...),
    campus = BicoccappCampusApi(...)
)

// Get student profile
val profile = bicoccapp.profile.getProfile(token)

// Get academic career
val career = bicoccapp.career.getCareer(matricId, personId, studentId)

// Get exam sessions
val exams = bicoccapp.exams.getExamsSessions(matricId, personId)

// Get campus points of interest
val pois = bicoccapp.campus.getPointsOfInterest()
```

## Authentication

The API uses token-based authentication with three headers:
- `uid` - User identifier (email address)
- `client` - Client session token
- `access-token` - Access token for authorization

Tokens are obtained through the OpenID Connect flow with the university's identity provider.

## Dependencies

- Ktor 3.3 (HTTP client)
- Ktorfit 2.7 (type-safe HTTP)
- Kotlinx Serialization (JSON)
