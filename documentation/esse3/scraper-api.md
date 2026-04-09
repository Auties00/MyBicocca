# Esse3 Scraper API Reference

The `esse3-scraper` module is the primary data source for ESSE3 features in MyBicocca. It scrapes the ESSE3 web interface using Ktor HTTP client and JSoup HTML parsing.

**Module path:** `data-api/esse3-scraper/`
**Package:** `it.attendance100.mybicocca.data.api.esse3`
**Base URL:** `https://s3w.si.unimib.it`

## Entry Point: `Esse3Api`

```kotlin
class Esse3Api(
    sessionCookies: List<Cookie>,
    httpClientConfig: HttpClientConfig<*>.() -> Unit = {}
) : AutoCloseable
```

Requires Shibboleth SSO session cookies obtained externally. All sub-APIs share a single `HttpClient` with 30-second timeouts and `followRedirects = true`.

### Sub-API Properties

| Property | Type | Description |
|----------|------|-------------|
| `auth` | `Esse3AuthApi` | Logout operations |
| `profile` | `Esse3ProfileApi` | Personal data, address, contacts, photo |
| `career` | `Esse3CareerApi` | Academic record (libretto), study plan, course details |
| `exams` | `Esse3ExamsApi` | Exam sessions, reservations, results |
| `taxes` | `Esse3TaxesApi` | Tax bills, payments, PagoPA |
| `internships` | `Esse3InternshipApi` | Internship opportunities, applications, companies |
| `questionnaires` | `Esse3QuestionnaireApi` | Teaching evaluation (ValDid) questionnaires |

---

## `Esse3AuthApi`

### `logout()`
Invalidates the current session.

```kotlin
suspend fun logout(): Unit
```

**Login URL constant:**
```kotlin
const val ESSE3_LOGIN_URL = "https://s3w.si.unimib.it/auth/studente/HomePageStudente.do"
```

---

## `Esse3ProfileApi`

Manages student profile operations: personal data, residence, contacts, and photo.

### `getPhoto()`
Downloads the student's ID photo as a byte stream.

```kotlin
suspend fun getPhoto(): ByteReadChannel
```

**Returns:** Raw image bytes (JPEG/PNG).
**Throws:** `ApiRequestException` if non-200 status.

### `getPersonalData()`
Retrieves the student's registry (Anagrafica) from the personal data page.

```kotlin
suspend fun getPersonalData(): Esse3PersonalData
```

**Returns:** `Esse3PersonalData` with name, surname, sex, birth date, citizenship, birth location, fiscal code.
**Throws:** `HtmlParsingException` if required fields are missing.

### `getResidenceAddressOptions()`
Gets valid dropdown options for the address form (countries, provinces, cities).

```kotlin
suspend fun getResidenceAddressOptions(): Esse3ResidenceAddressOptions
```

**Returns:** `Esse3ResidenceAddressOptions` containing three `List<String>` for form dropdowns.

### `getResidenceAddress()`
Reads the current residence address from the platform.

```kotlin
suspend fun getResidenceAddress(): Esse3ResidenceAddress
```

**Returns:** `Esse3ResidenceAddress` with country, province, city, zip, street, phone, etc.

### `updateResidenceAddress(address)`
Submits a new residence address via form POST.

```kotlin
suspend fun updateResidenceAddress(address: Esse3ResidenceAddress): Unit
```

**Throws:** `HtmlParsingException` if the country/province/city doesn't match platform options.

**Note:** Uses XPath-style form field names like:
```
/WS/DataSet[@LocalEntityName='P01_ANAPER_ANAG_WEB']/Row[@Num='1']/naz_res_id
```

### `getContactInfo()`
Retrieves current contact information (email, phone, delivery preferences).

```kotlin
suspend fun getContactInfo(): Esse3ContactInfo
```

**Returns:** `Esse3ContactInfo` with document/tax delivery type, email, fax, mobile, privacy consent.

### `updateContactInfo(contactInfo)`
Updates contact information. Parses the mobile number using `libphonenumber`.

```kotlin
suspend fun updateContactInfo(contactInfo: Esse3ContactInfo): Unit
```

**Throws:** `IllegalArgumentException` if phone number cannot be parsed.

---

## `Esse3CareerApi`

Academic career operations: record book (libretto), study plan, course details, exam attempts.

### `getStudyPlan()`
Retrieves the student's study plan with status, metadata, and all planned courses organized by year.

```kotlin
suspend fun getStudyPlan(): Esse3StudyPlan
```

**Returns:** `Esse3StudyPlan` with status (APPROVED/PENDING/REJECTED/DRAFT), type, dates, and `List<Esse3PlannedCourse>`.

### `printStudyPlan()`
Downloads the study plan as a PDF document.

```kotlin
suspend fun printStudyPlan(): ByteReadChannel
```

**Returns:** PDF bytes.

### `getAcademicRecord()`
Retrieves the full academic record (libretto) including all courses, grades, and GPA calculations.

```kotlin
suspend fun getAcademicRecord(): Esse3AcademicRecord
```

**Returns:** `Esse3AcademicRecord` with:
- `courses: List<Esse3Course>` - All courses with codes, names, years, credits, grades, exam dates
- `unweightedGpa: Double` - Arithmetic mean (Media Aritmetica)
- `weightedGpa: Double` - Weighted mean by credits (Media Ponderata)

**Grade parsing:** Grades are parsed into `Esse3Grade` sealed class (Numeric 18-30, 30L, Passed/IDO, Approved/APPR, Absent, Failed, Withdrawn).

### `getCourseInfo(course)`
Gets detailed information about a specific course including didactic units.

```kotlin
suspend fun getCourseInfo(course: Esse3Course): Esse3CourseDetails
```

**Parameters:** An `Esse3Course` object (from `getAcademicRecord()`).
**Returns:** `Esse3CourseDetails` with teaching activity info, degree program info, status, grade, and `List<Esse3CourseUnit>`.

### `getCourseExamAttempts(course)`
Gets the history of all exam attempts for a course.

```kotlin
suspend fun getCourseExamAttempts(course: Esse3Course): List<Esse3ExamAttempt>
```

**Returns:** List of attempts with date, type, outcome (Passed/Failed/Absent/Withdrawn/Booked), and verbalization date. Returns empty list if no attempts URL is available.

---

## `Esse3ExamsApi`

Exam session browsing, booking, cancellation, and history.

### `getAvailableExamSessions()`
Lists all exam sessions currently available for booking.

```kotlin
suspend fun getAvailableExamSessions(): List<Esse3ExamSession>
```

**Returns:** List of `Esse3ExamSession` with course name, exam date, registration window dates, description, exam mode (IN_PERSON/REMOTE), academic years, and info path for details.

### `getExamSessionInfo(session)`
Gets complete details for a specific exam session including location, teachers, and registration count.

```kotlin
suspend fun getExamSessionInfo(session: Esse3ExamSession): Esse3ExamSessionInformation
```

**Returns:** `Esse3ExamSessionInformation` with teaching activity, description, session list, exam type (Written/Oral/Partial/Other), teachers, datetime, building, room, and registration number.

### `reserveExamSession(session, notes)`
Books an exam session. Navigates to the info page, fills the form, and submits.

```kotlin
suspend fun reserveExamSession(session: Esse3ExamSession, notes: String = ""): Unit
```

**Parameters:**
- `session` - The exam session to book
- `notes` - Optional notes for the teacher (default empty)

**Throws:** `HtmlParsingException` if booking fails (e.g., "Attenzione" error message).

### `getExamReservations()`
Gets all currently booked exams.

```kotlin
suspend fun getExamReservations(): List<Esse3ExamReservation>
```

**Returns:** List of `Esse3ExamReservation` with reservation number, max count, mode, type, teachers, datetime, building, room, and session/activity IDs for cancellation.

### `cancelExamReservation(reservation)`
Cancels a booked exam. Navigates to the reservation, finds the cancel button, confirms cancellation via form.

```kotlin
suspend fun cancelExamReservation(reservation: Esse3ExamReservation): Unit
```

**Throws:** `HtmlParsingException` if cancellation button not found or registration is closed.

### `printExamReservation(reservation)`
Downloads an exam reservation receipt as PDF.

```kotlin
suspend fun printExamReservation(reservation: Esse3ExamReservation): ByteReadChannel
```

### `getExamReservationsHistory()`
Gets the complete history of all reservation operations (bookings and cancellations).

```kotlin
suspend fun getExamReservationsHistory(): List<Esse3CourseReservationHistory>
```

**Returns:** List grouped by course, each containing `List<Esse3ReservationHistoryEntry>` with operation datetime, exam info, operation type (RESERVED/CANCELLED), and who performed it.

### `getExamResults()`
Gets exam results. **NOTE: This method is not yet implemented (`TODO()`).**

```kotlin
suspend fun getExamResults(): List<Esse3ExamResult>
```

---

## `Esse3TaxesApi`

Tax bills, payment status, PagoPA integration.

### `getTaxBills()`
Lists all tax bills (fatture) with their payment status.

```kotlin
suspend fun getTaxBills(): List<Esse3TaxBill>
```

**Returns:** List of `Esse3TaxBill` with id, invoice number, description, due date, amount (`BigDecimal`), payment status, and PagoPA availability flag.

### `getTaxBillDetail(bill)`
Gets itemized detail for a specific bill, including PagoPA payment information.

```kotlin
suspend fun getTaxBillDetail(bill: Esse3TaxBill): Esse3TaxBillDetail
```

**Returns:** `Esse3TaxBillDetail` with:
- `items: List<Esse3TaxBillItem>` - Line items (academic year, installment, description, amount)
- `paymentMethod: Esse3PaymentMethod` - PagoPA, BankTransfer, or Other
- `pagoPAInfo: Esse3PagoPAInfo?` - Notice code, IUV, RPT ID, status, transaction outcome

### `downloadPaymentReceipt(detail)`
Downloads a PagoPA payment receipt PDF.

```kotlin
suspend fun downloadPaymentReceipt(detail: Esse3TaxBillDetail): ByteReadChannel?
```

**Returns:** PDF bytes, or `null` if no RPT ID is available.

### `refreshPaymentStatus()`
Triggers a payment status refresh check with the payment system, then returns the updated bill list.

```kotlin
suspend fun refreshPaymentStatus(): List<Esse3TaxBill>
```

---

## `Esse3InternshipApi`

Internship opportunity search, applications, companies, and active internships.

### `searchOpportunities(searchText, type?, sector?, campaignId?, disciplineAreaId?)`
Searches for internship opportunities with optional filters.

```kotlin
suspend fun searchOpportunities(
    searchText: String,
    type: Esse3InternshipType? = null,
    sector: String? = null,
    campaignId: Long? = null,
    disciplineAreaId: Long? = null
): List<Esse3InternshipOpportunity>
```

**Returns:** List of `Esse3InternshipOpportunity` with id, title, company, type, application deadline, saved status.

### `getOpportunityDetail(opportunity)`
Gets full details for an internship opportunity.

```kotlin
suspend fun getOpportunityDetail(opportunity: Esse3InternshipOpportunity): Esse3InternshipOpportunityDetail
```

**Returns:** `Esse3InternshipOpportunityDetail` with description, objectives, location, functional area, benefits, expected start/duration, requirements (career types, languages), saved status.

### `getSavedOpportunities()`
Gets the student's saved/favorite opportunities.

```kotlin
suspend fun getSavedOpportunities(): List<Esse3InternshipOpportunity>
```

### `saveOpportunity(opportunity)` / `unsaveOpportunity(opportunity)`
Adds/removes an opportunity from favorites. No-ops if already in desired state. Checks current saved list before acting.

```kotlin
suspend fun saveOpportunity(opportunity: Esse3InternshipOpportunity): Unit
suspend fun unsaveOpportunity(opportunity: Esse3InternshipOpportunity): Unit
```

**Note:** `unsaveOpportunity` throws `HtmlParsingException` on HTTP 500 (duplicate saved entries).

### `getApplications()`
Gets the student's internship applications.

```kotlin
suspend fun getApplications(): List<Esse3InternshipApplication>
```

**Returns:** List with application ID, opportunity ID/title, company, type, status (Submitted/UnderReview/Accepted/Rejected/Withdrawn), and date.

### `getInternships()`
Gets the student's active and completed internships.

```kotlin
suspend fun getInternships(): List<Esse3Internship>
```

**Returns:** List with internship ID, opportunity ID, title, company info, start/end dates, status (Active/Completed/Suspended/Cancelled).

### `searchCompanies(companyName, sector?, onlyWithConvention?)`
Searches for companies in the internship system.

```kotlin
suspend fun searchCompanies(
    companyName: String,
    sector: String? = null,
    onlyWithConvention: Boolean = true
): List<Esse3Company>
```

### `getCompanyInformation(company)`
Gets detailed company information including locations and conventions.

```kotlin
suspend fun getCompanyInformation(company: Esse3Company): Esse3CompanyInformation
```

**Returns:** `Esse3CompanyInformation` with name, description, logo URL, locations (address/type/email), and conventions (dates, duration, auto-renewal).

### `getCompanyLogo(company)`
Downloads a company logo image.

```kotlin
suspend fun getCompanyLogo(company: Esse3Company): ByteReadChannel
```

### `getSavedSearches()` / `deleteSavedSearch(search)`
Manages saved internship search queries.

```kotlin
suspend fun getSavedSearches(): List<Esse3SavedSearch>
suspend fun deleteSavedSearch(search: Esse3SavedSearch): Unit
```

---

## `Esse3QuestionnaireApi`

Teaching evaluation questionnaires (ValDid - Valutazione Didattica).

### `getEvaluationCourses()`
Lists courses from the academic record that can be evaluated.

```kotlin
suspend fun getEvaluationCourses(): List<Esse3EvaluationCourse>
```

**Returns:** List with course code/name, year, credits, academic year, and status (NotAvailable/Pending/Completed).

### `getEvaluationPartitions(course)`
Gets the evaluation sections for a course. A course may have multiple partitions (different teachers, modules).

```kotlin
suspend fun getEvaluationPartitions(course: Esse3EvaluationCourse): List<Esse3EvaluationPartition>
```

**Returns:** List with unit name, teacher, activity type, partition, and status (NotAvailable/Pending/Completed). Returns empty list if course status is NotAvailable.

### `startQuestionnaire(partition)`
Begins filling out a questionnaire for a specific partition.

```kotlin
suspend fun startQuestionnaire(partition: Esse3EvaluationPartition): Esse3QuestionnairePage?
```

**Returns:** First questionnaire page, or `null` if partition is not Pending or no form found.

### `submitPage(page, answers)`
Submits answers for the current page and navigates forward.

```kotlin
suspend fun submitPage(
    page: Esse3QuestionnairePage,
    answers: Map<String, String>
): Esse3QuestionnaireSubmitResult
```

**Parameters:**
- `page` - Current page (contains navigation state)
- `answers` - Map of `fieldName -> value` for each question answered

**Returns:**
- `NextPage(page)` - Next page of questions
- `Completed` - Questionnaire finished
- `ValidationError(message)` - Validation failed

### `previousPage(page)`
Navigates back to the previous questionnaire page.

```kotlin
suspend fun previousPage(page: Esse3QuestionnairePage): Esse3QuestionnairePage
```

**Throws:** `HtmlParsingException` if on the first page.

### `exitQuestionnaire(page)`
Exits the questionnaire without completing it.

```kotlin
suspend fun exitQuestionnaire(page: Esse3QuestionnairePage): Unit
```

### Questionnaire Flow

```
getEvaluationCourses()
    |
    v
getEvaluationPartitions(course)
    |
    v
startQuestionnaire(partition) -> Page 1
    |
    v
submitPage(page, answers) -> NextPage / Completed / ValidationError
    |                              |
    v                              v
previousPage(page)           Done!
    |
    v
exitQuestionnaire(page)  (abort)
```

Question types per page:
- `FreeText` - Text input/textarea with optional max length
- `SingleChoice` - Radio buttons or dropdown
- `MultipleChoice` - Checkboxes
- `Rating` - Numeric scale (min-max)
