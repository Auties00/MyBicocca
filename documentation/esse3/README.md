# Esse3 Data Modules Documentation

Documentation for the Esse3 data-api modules used by MyBicocca to interact with the university's ESSE3 student records system.

## Module Overview

There are **two separate modules** that provide access to ESSE3:

| Module | Type | Base URL | Auth Method |
|--------|------|----------|-------------|
| `esse3` | REST API client | `https://s3w.si.unimib.it/e3rest/api` | Cookie-based + JWT |
| `esse3-scraper` | HTML web scraper | `https://s3w.si.unimib.it` | Shibboleth SSO session cookies |

### Which module to use?

- **`esse3-scraper`** is the primary module used by MyBicocca. It provides rich, student-facing data by scraping the actual ESSE3 web interface. It covers: profile, academic record, exams, taxes, internships, and questionnaires.
- **`esse3` (REST)** wraps the official ESSE3 REST API. It has broader endpoint coverage (27+ service facades) but many endpoints require elevated permissions (admin, teacher). Useful for specific REST-only operations.

## Documentation Files

| File | Description |
|------|-------------|
| [scraper-api.md](scraper-api.md) | Complete esse3-scraper API reference (the primary module for app features) |
| [rest-api.md](rest-api.md) | esse3 REST API reference (supplementary module) |
| [data-types.md](data-types.md) | All DTO types, sealed classes, and enums |
| [authentication.md](authentication.md) | Authentication flows and session management |
| [error-handling.md](error-handling.md) | Exceptions, error patterns, and edge cases |
| [app-integration.md](app-integration.md) | Guide for implementing ESSE3 features in the MyBicocca app |

## Quick Start

```kotlin
// esse3-scraper (primary - requires session cookies from Shibboleth SSO)
val api = Esse3Api(sessionCookies = cookies)
val record = api.career.getAcademicRecord()
val exams = api.exams.getAvailableExamSessions()
api.close()

// esse3 REST (supplementary - cookie-based auth)
val restApi = Esse3Api()
restApi.auth.login()
val careers = restApi.careers.getCareers()
restApi.close()
```

## Package Structure

```
it.attendance100.mybicocca.data
  .api.esse3/          -- API facade classes (both modules share this package)
  .dto.esse3/          -- DTO types (shared package, different classes per module)
  .exception.esse3/    -- Custom exceptions (REST module only)
  .common/             -- Shared utilities (URL building, HTML parsing, etc.)
```
