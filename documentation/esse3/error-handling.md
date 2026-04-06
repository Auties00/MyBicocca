# Esse3 Error Handling

## Exception Hierarchy

### Scraper Module Exceptions

The scraper uses two exception types from the `data.common.exception` package:

#### `HtmlParsingException`
Thrown when HTML parsing fails - missing elements, unexpected page structure, invalid data formats.

```kotlin
class HtmlParsingException(message: String) : Exception(message)
```

Common causes:
- Required HTML element not found (table, form, input)
- Data field missing from a parsed grid/table
- Invalid date format that can't be parsed
- Error pages from ESSE3 ("Pagina non trovata", "Dati non trovati in sessione")
- Form submission errors ("Attenzione" messages)
- Invalid dropdown values (country/province/city not in list)

#### `ApiRequestException`
Thrown for HTTP-level errors.

```kotlin
class ApiRequestException(
    val statusCode: Int,
    message: String? = null
) : Exception(message)
```

### REST Module Exceptions

Located in `data.exception.esse3`:

#### `Esse3NotAuthorizedException`
Thrown on HTTP 403 (Forbidden).

```kotlin
class Esse3NotAuthorizedException(
    val expectedPermissionLevels: Set<Esse3PermissionLevel>,
    val apiErrorMessage: String? = null
) : ApiRequestException(HttpStatusCode.Forbidden.value)
```

Use `expectedPermissionLevels` to show the user which role is required.

#### `Esse3ValidationException`
Thrown on HTTP 422 (Unprocessable Entity) or other non-success status codes.

```kotlin
class Esse3ValidationException(
    val errorResponse: Esse3ErrorResponse? = null
) : ApiRequestException(errorResponse?.statusCode ?: HttpStatusCode.UnprocessableEntity.value)
```

The `errorResponse` contains structured error details:
```kotlin
data class Esse3ErrorResponse(
    val statusCode: Int?,
    val returnCode: Int?,
    val errorMessage: String?,
    val errorDetails: List<Esse3ErrorDetail>
)

data class Esse3ErrorDetail(
    val errorType: String?,
    val value: String?,
    val rawValue: String?
)
```

## Error Patterns in the Scraper

### Error Page Detection

The `Esse3AbstractApi.checkForErrorPage()` method automatically checks every HTML response for error pages:

```kotlin
// Checked conditions:
// 1. Page title contains "Messaggio" -> it's an error page
// 2. H2 contains "Pagina non trovata" -> page not found
// 3. H2 contains "Dati non trovati in sessione" -> session flow broken
```

**"Dati non trovati in sessione"** is the most common error. It means the page requires proper navigation flow (visiting an entry point URL with `menu_opened_cod` parameter first). The scraper handles this by always starting from the entry point URL before accessing sub-pages.

### Form Submission Errors

After form submissions, errors are detected via:

```kotlin
// Update operations (profile, address, contact):
checkForUpdateError(response)
// Checks for: HTTP non-200 status, #alertError element

// Exam reservation:
document.selectFirst("#app-text_esito_pren_msg")
// Error if text contains "Attenzione"

// Exam cancellation:
document.selectFirst(".alert-danger, .errore, #error")
// Error if text is not blank

// Questionnaire submission:
document.selectFirst(".alert-danger, .errore, #error")
// Returns ValidationError result instead of throwing
```

### Empty Data Handling

Some methods return empty collections instead of throwing when data is not found:

| Method | Empty behavior |
|--------|----------------|
| `getAvailableExamSessions()` | Returns `emptyList()` if table not found |
| `getCourseExamAttempts()` | Returns `emptyList()` if no attempts URL |
| `getApplications()` | Returns `emptyList()` if data table not found |
| `searchCompanies()` | Returns `emptyList()` if table not found |
| `getEvaluationPartitions()` | Returns `emptyList()` if course status is NotAvailable |
| `startQuestionnaire()` | Returns `null` if partition not Pending |

### REST Module Error Behavior

The REST `Esse3AbstractApi` handles responses as:

| HTTP Status | Behavior |
|-------------|----------|
| 200-299 | Success - deserialize response |
| 403 | Throw `Esse3NotAuthorizedException` |
| 404 | Return empty list (list methods) or throw |
| 422 | Throw `Esse3ValidationException` with error details |
| Other | Throw `Esse3ValidationException` |

## Recommended Error Handling in the App

```kotlin
// In a ViewModel
try {
    val record = api.career.getAcademicRecord()
    _state.value = State.Success(record)
} catch (e: HtmlParsingException) {
    // UI parsing issue - show user-friendly error
    // Log the technical details for debugging
    _state.value = State.Error("Unable to load academic record")
} catch (e: ApiRequestException) {
    if (e.statusCode == 401 || e.statusCode == 403) {
        // Session expired - redirect to login
        _state.value = State.SessionExpired
    } else {
        _state.value = State.Error("Server error (${e.statusCode})")
    }
} catch (e: Exception) {
    // Network errors (timeout, DNS, etc.)
    _state.value = State.Error("Connection error")
}
```

## Date/Time Parsing

The scraper uses strict date formats. Parsing failures return `null` rather than throwing:

```kotlin
// Date format: dd/MM/yyyy (e.g., "05/04/2026")
Esse3AbstractApi.parseDate(value: String): LocalDate?

// DateTime format: dd/MM/yyyy HH:mm (e.g., "05/04/2026 14:30")
Esse3AbstractApi.parseDateTime(value: String): LocalDateTime?
// Falls back to parseDate().atStartOfDay() if time part is missing
```

When a null date is unexpected, the scraper methods throw `HtmlParsingException` with context about which field had the invalid date.
