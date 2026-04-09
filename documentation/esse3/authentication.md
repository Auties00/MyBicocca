# Esse3 Authentication

## Overview

ESSE3 uses two separate authentication mechanisms depending on the module:

| Module | Auth Method | Flow |
|--------|-------------|------|
| `esse3-scraper` | Shibboleth SSO session cookies | External browser/WebView login -> extract cookies -> pass to `Esse3Api` |
| `esse3` (REST) | Cookie-based + optional JWT | Call `auth.login()` on the REST client directly |

## Scraper Authentication (Primary)

The scraper requires session cookies obtained externally through the Shibboleth SSO flow. The app must:

1. Open a WebView/browser to `ESSE3_LOGIN_URL`:
   ```kotlin
   const val ESSE3_LOGIN_URL = "https://s3w.si.unimib.it/auth/studente/HomePageStudente.do"
   ```

2. The user authenticates through the university's IdP (Identity Provider)

3. After successful login, extract session cookies (primarily `JSESSIONID`) from the WebView/browser

4. Pass the cookies to create an API instance:
   ```kotlin
   val api = Esse3Api(sessionCookies = extractedCookies)
   ```

5. All subsequent API calls use these cookies automatically via Ktor's `HttpCookies` plugin

### Cookie Requirements

- Cookies must include the `domain` property for Ktor to apply them correctly
- The `JSESSIONID` cookie is the critical session identifier
- Additional Shibboleth cookies (`_shibsession_*`, `_idp_session`) may be present but are consumed during the SSO redirect
- Session cookies expire after server-side timeout (typically 30 minutes of inactivity)

### Session Lifecycle

```
Browser opens ESSE3_LOGIN_URL
    |
    v
Redirect to IdP (idp.unimib.it)
    |
    v
User enters credentials
    |
    v
IdP redirects back to ESSE3 with SAML assertion
    |
    v
ESSE3 sets JSESSIONID cookie
    |
    v
Extract cookies -> Esse3Api(sessionCookies)
    |
    v
API calls authenticated via cookie
    |
    v
api.auth.logout() or session timeout
```

### Logout

```kotlin
api.auth.logout()  // GET /Logout.do - invalidates server session
api.close()        // Closes HTTP client
```

## REST API Authentication

The REST module uses cookie-based auth with optional JWT support:

### Basic Login
```kotlin
val api = Esse3Api()
val session: Esse3UserSession = api.auth.login()
// session.user contains user info
// session.authToken contains the auth token
// session.jwt contains JWT if available
// Cookies are stored automatically in AcceptAllCookiesStorage
```

### JWT Operations
```kotlin
// Get a new JWT
val jwt: Esse3JWTModel = api.auth.getJWT()

// Refresh an existing JWT
val refreshed: Esse3JWTModel = api.auth.refreshJWT(jwt.jwt)

// Get public keys for JWT verification
val jwk: Esse3JWKModel = api.auth.getJWK()
```

### Session Checks
```kotlin
// Check if currently logged in
val check: Esse3CheckLoginResult = api.auth.checkLogon()
// check.ok = true if logged in
// check.changePassword = true if password change required

// Validate a specific session
api.auth.checkSessionId(sessionId = "abc123")
```

### User Session Data

On login, the REST API returns an `Esse3UserSession` containing:

```kotlin
data class Esse3UserSession(
    val user: Esse3User,           // User details (name, ID, group, career segments)
    val authToken: String?,
    val internalAuthToken: String?,
    val expPwd: String?,           // Password expiration
    val credentials: Esse3AuthenticationCredentials?,
    val jwt: String?,
    val profili: List<Esse3UserProfile>  // Available profiles/roles
)

data class Esse3User(
    val firstName: String?,
    val lastName: String?,
    val sex: String?,
    val codFis: String?,           // Fiscal code
    val persId: Long?,             // Person ID
    val userId: String?,
    val grpDes: String?,           // Group description (e.g., "STUDENTE")
    val trattiCarriera: List<Esse3CareerSegmentKeys>?  // Career segments
    // ... additional fields
)
```

### Permission System

The REST API enforces permission levels per endpoint:

```kotlin
enum class Esse3PermissionLevel {
    ANY,
    AUTHENTICATED_USER,
    STUDENT,                        // groupId = 6
    PROVISIONAL_ENROLLED_STUDENT,   // groupId = 4
    TEACHER,                        // groupId = 7
    ADMIN_OFFICE,
    ADMIN_OFFICE_ADMIN,
    EXTERNAL_SUBJECT,
    TECHNICAL_USER,
    REGISTERED_USER,                // groupId = 9
    UNKNOWN
}
```

When a student calls a teacher-only endpoint, the API returns HTTP 403 and the module throws `Esse3NotAuthorizedException` with the expected permission levels.

## Testing Authentication

Integration tests use the `GlobalApiData` JUnit 5 extension:

```kotlin
// Environment variables required:
// BICOCCA_USERNAME - University email/username
// BICOCCA_PASSWORD - University password

// The esse3-scraper tests use Selenium WebDriver to perform
// the full Shibboleth SSO flow in a headless browser
```
