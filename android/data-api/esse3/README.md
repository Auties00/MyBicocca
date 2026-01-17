# Esse3 API Module

Kotlin API client for [Segreterie OnLine](https://s3w.si.unimib.it/) (Esse3), the student administration portal.

## Overview

This module provides a Kotlin client for interacting with the Esse3 student portal, powered by [CINECA](https://www.cineca.it/). Unlike modern REST APIs, Esse3 is a traditional server-rendered web application, so this client works entirely through HTML scraping.

## Key Characteristics

- **Session-based authentication** - Uses cookies (JSESSIONID) obtained via Shibboleth SSO
- **HTML scraping** - All data is extracted by parsing HTML pages with JSoup
- **Navigation flow required** - Many pages require proper entry points with `menu_opened_cod` parameter
- **Form-based submission** - Actions use HTML form POST requests

## Authentication

Authentication is handled externally via Shibboleth SSO. The login flow:

1. Open `ESSE3_LOGIN_URL` in a browser/WebView
2. User authenticates via university SSO (Shibboleth)
3. Extract session cookies after successful login
4. Pass cookies to `Esse3Api` constructor

```kotlin
// After SSO authentication, extract cookies from the browser/WebView
val sessionCookies: List<Cookie> = ... // extracted from browser

// Create API instance with session cookies
val api = Esse3Api(sessionCookies)

// Now you can use the API
val record = api.career.getAcademicRecord()
```

## API Coverage

| Sub-API          | Description                                   |
|------------------|-----------------------------------------------|
| `auth`           | Logout and session management                 |
| `profile`        | Homepage, profile photo, personal information |
| `career`         | Academic record (libretto), study plan        |
| `exams`          | Exam sessions, reservations, results          |
| `taxes`          | Tax bills, payments, MAV/PagoPA documents     |
| `internships`    | Stage opportunities, applications             |
| `questionnaires` | Course evaluations, surveys                   |

## Usage

```kotlin
val api = Esse3Api(sessionCookies)

// Get academic record (libretto)
val record = api.career.getAcademicRecord()
println("Weighted GPA: ${record.weightedGpa}")
println("Unweighted GPA: ${record.unweightedGpa}")
record.courses.forEach { course ->
    println("${course.code} - ${course.name}: ${course.grade}")
}

// Get study plan
val plan = api.career.getStudyPlan()

// Get course details
val details = api.career.getCourseInfo(course)

// Get exam attempts history
val attempts = api.career.getCourseExamAttempts(course)

// Get tax bills
val bills = api.taxes.getBillList()

// Search internships
val internships = api.internships.search("software")

// Get pending questionnaires
val questionnaires = api.questionnaires.getPending()

// Logout
api.auth.logout()
api.close()
```

## Implementation Details

### HTML Scraping Approach

Since Esse3 has no public API, all data is extracted by:

1. Making HTTP requests with session cookies
2. Parsing HTML responses with JSoup
3. Extracting data from tables, forms, and text patterns
4. Using regex for complex text parsing (grades, dates, status)

### Page Navigation

Esse3 requires proper navigation flow. Direct access to some pages fails with "Dati non trovati in sessione" (Session data not found). The client handles this by:

- Using menu entry point URLs with `menu_opened_cod` parameter
- Following the expected navigation sequence
- Extracting hidden form fields for POST submissions

### Data Formats

- **Dates**: `DD/MM/YYYY` format
- **Grades**: Numeric (18-30), "30L" (30 cum laude), or text ("Idoneo", "Approvato")
- **Credits**: Integer CFU values

## Parsed Pages

| Page           | URL Pattern                                           | Data Extracted          |
|----------------|-------------------------------------------------------|-------------------------|
| Libretto       | `/auth/studente/Libretto/LibrettoHome.do`             | Courses, grades, GPA    |
| Study Plan     | `/auth/studente/Piani/PianiHome.do`                   | Planned courses, status |
| Course Details | `/auth/studente/Libretto/LibrettoADContestualizza.do` | Units, exam info        |
| Exam Attempts  | `/auth/studente/Libretto/ProveEsamiSuperatiList.do`   | Attempt history         |
| Tax Bills      | `/auth/studente/Tasse/BollettinoList.do`              | Bills, payments         |
| Internships    | `/auth/studente/Tirocini/RicercaOfferta.do`           | Opportunities           |

## Dependencies

- Ktor 3.3 (HTTP client with cookie support)
- Jsoup 1.22 (HTML parsing)
