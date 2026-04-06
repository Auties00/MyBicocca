# Esse3 REST API Reference

The `esse3` module wraps the official ESSE3 REST API. It has broad endpoint coverage but many endpoints require elevated permissions (teacher, admin) and are not usable by regular students.

**Module path:** `data-api/esse3/`
**Package:** `it.attendance100.mybicocca.data.api.esse3`
**Base URL:** `https://s3w.si.unimib.it/e3rest/api`

## Entry Point: `Esse3Api`

```kotlin
class Esse3Api(
    httpClientConfig: HttpClientConfig<*>.() -> Unit = {}
) : AutoCloseable
```

Uses `AcceptAllCookiesStorage()` for session management and kotlinx.serialization with lenient JSON parsing (`ignoreUnknownKeys`, `coerceInputValues`, `isLenient`).

## Service Facades

All service facades inherit from `Esse3AbstractApi(client, json, serviceBasePath)`.

Each method accepts an `expectedPermissionLevels: Set<Esse3PermissionLevel>` parameter specifying who can call it. The API throws `Esse3NotAuthorizedException` on 403 responses.

### Student-Relevant Services

These are the services most useful for the MyBicocca app (student context):

| Property | Class | Service Path | Key Operations |
|----------|-------|-------------|----------------|
| `auth` | `Esse3AuthApi` | `/` | Login, logout, JWT, check session, change password |
| `careers` | `Esse3CareersApi` | `/carriere-service-v1` | Career data, enrollments, attendance |
| `transcript` | `Esse3TranscriptApi` | `/libretto-service-v1` | Academic transcript/record book |
| `plans` | `Esse3PlansApi` | `/piani-service-v1` | Study plans |
| `tuitionFees` | `Esse3TuitionFeesApi` | `/tasse-service-v1` | Tuition fee information |
| `personalData` | `Esse3PersonalDataApi` | `/persone-service-v1` | Personal information, addresses |
| `badge` | `Esse3BadgeApi` | `/badge-service-v1` | Student badge/ID card |
| `internships` | `Esse3InternshipsApi` | `/tirocini-service-v1` | Internship information |
| `questionnaires` | `Esse3QuestionnairesApi` | `/questionari-service-v1` | Questionnaires |
| `communications` | `Esse3CommunicationsApi` | `/comunicazioni-service-v1` | University announcements |
| `appointmentsCalendar` | `Esse3AppointmentsCalendarApi` | `/calendario-appuntamenti-service-v1` | Appointment scheduling |
| `attachments` | `Esse3AttachmentsApi` | `/allegati-service-v1` | File attachments/documents |
| `users` | `Esse3UsersApi` | `/utenti-service-v1` | User information |

### Administrative/Teacher Services

These require elevated permissions and are generally not accessible to students:

| Property | Class | Service Path |
|----------|-------|-------------|
| `teachers` | `Esse3TeachersApi` | `/docenti-service-v1` |
| `teacherReporting` | `Esse3TeacherReportingApi` | `/docenti-service-v1` |
| `records` | `Esse3RecordsApi` | `/verbali-service-v1` |
| `offer` | `Esse3OfferApi` | `/offerta-service-v1` |
| `structure` | `Esse3StructureApi` | `/strutture-service-v1` |
| `services` | `Esse3ServicesApi` | `/servizi-service-v1` |
| `competitions` | `Esse3CompetitionsApi` | `/bandi-service-v1` |
| `countries` | `Esse3CountriesApi` | `/paesi-service-v1` |
| `choiceRules` | `Esse3ChoiceRulesApi` | `/regole-scelta-service-v1` |
| `ruleProperties` | `Esse3RulePropertiesApi` | `/proprieta-regole-service-v1` |
| `logistics` | `Esse3LogisticsApi` | `/logistica-service-v1` |
| `calesa` | `Esse3CalesaApi` | `/calesa-service-v1` |
| `careerUpdate` | `Esse3CareerUpdateApi` | `/aggiornamento-carriera-service-v1` |
| `degreeAward` | `Esse3DegreeAwardApi` | `/assegnamento-laurea-service-v1` |
| `badgeImport` | `Esse3BadgeImportApi` | `/badge-service-v1` |
| `logging` | `Esse3LoggingApi` | `/logging-service-v1` |

## Auth API Details

The REST auth API provides more granular authentication than the scraper:

```kotlin
// Login and get session
auth.login(sessionLanguageCode?, optionalFields?): Esse3UserSession

// Check if logged in
auth.checkLogon(): Esse3CheckLoginResult  // { ok: Boolean, changePassword: Boolean }

// JWT operations
auth.getJWT(): Esse3JWTModel              // Get new JWT
auth.refreshJWT(jwt?): Esse3JWTModel      // Refresh existing JWT
auth.getJWK(): Esse3JWKModel              // Get public keys

// Session management
auth.logout(): Unit
auth.checkSessionId(sessionId): Unit
auth.getLanguageCode(): Esse3SessionLanguage
auth.setLanguageCode(code?): Esse3SessionLanguage

// Password management
auth.changeUserPassword(params): Esse3ChangePasswordResult

// Cache configuration
auth.getCacheParameters(): Esse3CacheInfo
auth.setCacheParameters(info): Esse3CacheInfo
```

## Base API Methods

`Esse3AbstractApi` provides typed HTTP methods for all verbs:

- `executeGet<T>()`, `executePost<T>()`, `executePut<T>()`, `executeDelete<T>()`, `executePatch<T>()` - Single object responses
- `executeJsonGetList<T>()`, etc. - List responses
- `executeStreamGet()`, etc. - Streaming/binary responses

**Error handling:**
- HTTP 403 -> `Esse3NotAuthorizedException` (with expected permission levels)
- HTTP 404 -> Returns empty list (for list methods)
- HTTP 422 -> `Esse3ValidationException` (with `Esse3ErrorResponse` details)
- Other non-success -> `Esse3ValidationException`

## Permission Levels

```kotlin
enum class Esse3PermissionLevel(val profileName: String?, val groupId: Int?) {
    ANY(null, null)
    AUTHENTICATED_USER("AUTHENTICATED", null)
    STUDENT("STUDENTE", 6)
    PROVISIONAL_ENROLLED_STUDENT("IMMATRICOLATI_IN_IPOTESI", 4)
    TEACHER("DOCENTE", 7)
    ADMIN_OFFICE("SEGRETERIA", null)
    ADMIN_OFFICE_ADMIN("SEGRETERIA_ADMIN", null)
    EXTERNAL_SUBJECT("SOGG_EST", null)
    TECHNICAL_USER("USER_TECNICO", null)
    REGISTERED_USER("REGISTRATO", 9)
    UNKNOWN(null, null)
}
```

## Custom Serializers

The REST module uses custom kotlinx.serialization serializers for ESSE3's data formats:

| Serializer | Format | Example |
|------------|--------|---------|
| `Esse3LocalDateSerializer` | `dd/MM/yyyy` | `"05/04/2026"` |
| `Esse3LocalDateTimeSerializer` | `dd/MM/yyyy HH:mm:ss` | `"05/04/2026 14:30:00"` |
| `Esse3IntBooleanSerializer` | `0`/`1` -> Boolean | `1` -> `true` |
| `Esse3EnumValueSerializer` | `{"value":"X"}` -> `"X"` | `{"value":"IT"}` -> `"IT"` |
