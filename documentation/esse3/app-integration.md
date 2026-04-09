# Esse3 App Integration Guide

Guide for implementing ESSE3 features in the MyBicocca Android app.

## Feature Inventory

Here's every feature the esse3-scraper API supports, grouped by app screen/section:

### 1. Student Profile Screen

| Feature | API Call | Data Type |
|---------|----------|-----------|
| Student photo | `api.profile.getPhoto()` | `ByteReadChannel` (image bytes) |
| Personal data (name, birth, fiscal code) | `api.profile.getPersonalData()` | `Esse3PersonalData` |
| Residence address (read) | `api.profile.getResidenceAddress()` | `Esse3ResidenceAddress` |
| Residence address (edit) | `api.profile.updateResidenceAddress(addr)` | - |
| Address form dropdowns | `api.profile.getResidenceAddressOptions()` | `Esse3ResidenceAddressOptions` |
| Contact info (read) | `api.profile.getContactInfo()` | `Esse3ContactInfo` |
| Contact info (edit) | `api.profile.updateContactInfo(info)` | - |

**Implementation notes:**
- Photo returns raw bytes - use `BitmapFactory.decodeStream()` or Coil's `ByteArrayFetcher`
- Address update requires valid values from `getResidenceAddressOptions()` dropdowns
- Contact update uses `libphonenumber` to parse mobile numbers - send full international format
- Privacy consent (`privacyConsent`) is part of contact info

### 2. Academic Record Screen (Libretto)

| Feature | API Call | Data Type |
|---------|----------|-----------|
| Full record with GPA | `api.career.getAcademicRecord()` | `Esse3AcademicRecord` |
| Course detail | `api.career.getCourseInfo(course)` | `Esse3CourseDetails` |
| Exam attempt history | `api.career.getCourseExamAttempts(course)` | `List<Esse3ExamAttempt>` |

**Implementation notes:**
- `Esse3AcademicRecord` contains both `unweightedGpa` and `weightedGpa` - display both
- Courses with `grade == null` are not yet passed
- `Esse3Grade` sealed class needs display formatting:
  - `Numeric(28, false)` -> "28"
  - `Numeric(30, true)` -> "30L"
  - `Passed` -> "IDO" (Idoneo)
  - Use `grade.toString()` for display
- `examAttemptsUrlPath != null` means the course has recorded attempts
- Course status: `NotAttended` / `Attended(year)` / `Passed(year, attendedYear)`

### 3. Study Plan Screen

| Feature | API Call | Data Type |
|---------|----------|-----------|
| Study plan with courses | `api.career.getStudyPlan()` | `Esse3StudyPlan` |
| Print study plan (PDF) | `api.career.printStudyPlan()` | `ByteReadChannel` (PDF) |

**Implementation notes:**
- Plan status: APPROVED (green), PENDING (yellow), REJECTED (red), DRAFT (gray)
- Courses are organized by year - group `Esse3PlannedCourse` by `year` field
- PDF download: save to cache/Downloads and open with `ACTION_VIEW` intent

### 4. Exams Screen

| Feature | API Call | Data Type |
|---------|----------|-----------|
| Available exam sessions | `api.exams.getAvailableExamSessions()` | `List<Esse3ExamSession>` |
| Exam session details | `api.exams.getExamSessionInfo(session)` | `Esse3ExamSessionInformation` |
| Book an exam | `api.exams.reserveExamSession(session, notes)` | - |
| Booked exams list | `api.exams.getExamReservations()` | `List<Esse3ExamReservation>` |
| Cancel reservation | `api.exams.cancelExamReservation(reservation)` | - |
| Print reservation (PDF) | `api.exams.printExamReservation(reservation)` | `ByteReadChannel` |
| Reservation history | `api.exams.getExamReservationsHistory()` | `List<Esse3CourseReservationHistory>` |
| Exam results | `api.exams.getExamResults()` | **NOT IMPLEMENTED** (TODO) |

**Implementation notes:**
- Show registration window dates (`registrationStartDate`/`registrationEndDate`) to indicate when booking is open
- Exam mode: IN_PERSON vs REMOTE - display with appropriate icon
- Exam type: Written / Oral / Partial / Other
- Reservation has `reservationNumber` / `maxReservationsCount` for "X of Y registered" display
- `sessionId` and `teachingActivityId` are opaque IDs used internally for cancel/print
- The `getExamResults()` method is a TODO and will throw `NotImplementedError`

### 5. Taxes Screen

| Feature | API Call | Data Type |
|---------|----------|-----------|
| Tax bills list | `api.taxes.getTaxBills()` | `List<Esse3TaxBill>` |
| Bill detail | `api.taxes.getTaxBillDetail(bill)` | `Esse3TaxBillDetail` |
| Download payment receipt | `api.taxes.downloadPaymentReceipt(detail)` | `ByteReadChannel?` |
| Refresh payment status | `api.taxes.refreshPaymentStatus()` | `List<Esse3TaxBill>` |

**Implementation notes:**
- Amount is `BigDecimal` - format with `NumberFormat.getCurrencyInstance(Locale.ITALY)`
- Payment status display:
  - `PaidConfirmed` -> green check, "Pagato"
  - `PaidPending` -> yellow clock, "In attesa di conferma"
  - `Unpaid` -> red, "Da pagare"
  - `Overdue` -> red alert, "Scaduto"
  - `Cancelled` -> gray strikethrough
- `pagoPaAvailable` flag indicates whether PagoPA payment button should be shown
- PagoPA info includes `noticeCode` (Codice Avviso) which the user can use to pay via the PagoPA app
- Receipt download returns `null` if no RPT ID is available (payment not yet processed)
- `refreshPaymentStatus()` does a POST to trigger a server-side check, then returns updated bills

### 6. Internships Screen

| Feature | API Call | Data Type |
|---------|----------|-----------|
| Search opportunities | `api.internships.searchOpportunities(text, ...)` | `List<Esse3InternshipOpportunity>` |
| Opportunity detail | `api.internships.getOpportunityDetail(opp)` | `Esse3InternshipOpportunityDetail` |
| Saved opportunities | `api.internships.getSavedOpportunities()` | `List<Esse3InternshipOpportunity>` |
| Save/unsave opportunity | `api.internships.saveOpportunity(opp)` / `unsaveOpportunity(opp)` | - |
| My applications | `api.internships.getApplications()` | `List<Esse3InternshipApplication>` |
| My internships | `api.internships.getInternships()` | `List<Esse3Internship>` |
| Search companies | `api.internships.searchCompanies(name, ...)` | `List<Esse3Company>` |
| Company detail | `api.internships.getCompanyInformation(company)` | `Esse3CompanyInformation` |
| Company logo | `api.internships.getCompanyLogo(company)` | `ByteReadChannel` |
| Saved searches | `api.internships.getSavedSearches()` | `List<Esse3SavedSearch>` |
| Delete saved search | `api.internships.deleteSavedSearch(search)` | - |

**Implementation notes:**
- Opportunity `isSaved` is mutable (`var`) - updated in-place by save/unsave methods
- Duration in `Esse3InternshipOpportunityDetail` is `kotlin.time.Duration` - format as days/weeks/months
- Application status: Submitted -> UnderReview -> Accepted/Rejected (or Withdrawn)
- Internship status: Active / Completed / Suspended / Cancelled
- Company logo may fail (not all companies have logos) - handle with placeholder

### 7. Questionnaires Screen (ValDid)

| Feature | API Call | Data Type |
|---------|----------|-----------|
| Courses to evaluate | `api.questionnaires.getEvaluationCourses()` | `List<Esse3EvaluationCourse>` |
| Course partitions | `api.questionnaires.getEvaluationPartitions(course)` | `List<Esse3EvaluationPartition>` |
| Start questionnaire | `api.questionnaires.startQuestionnaire(partition)` | `Esse3QuestionnairePage?` |
| Submit page answers | `api.questionnaires.submitPage(page, answers)` | `Esse3QuestionnaireSubmitResult` |
| Go to previous page | `api.questionnaires.previousPage(page)` | `Esse3QuestionnairePage` |
| Exit questionnaire | `api.questionnaires.exitQuestionnaire(page)` | - |

**Implementation notes:**
- Three-level hierarchy: Courses -> Partitions -> Questionnaire pages
- Course status icons: NotAvailable (gray), Pending (orange), Completed (green check)
- Question types map to Compose UI:
  - `FreeText` -> `TextField` (respect `maxLength`)
  - `SingleChoice` -> `RadioButton` group or `DropdownMenu`
  - `MultipleChoice` -> `Checkbox` group
  - `Rating` -> `Slider` or star rating (use `minValue`/`maxValue`)
- Build `answers` map using `question.fieldName` as key and selected value as value
- Handle `ValidationError` result by showing error message and keeping the same page
- `canGoBack` / `canGoForward` control navigation button visibility

## Architecture Recommendations

### Repository Pattern

Each feature area should have a repository combining API + local cache:

```kotlin
class Esse3CareerRepository @Inject constructor(
    private val api: Esse3Api,
    private val dao: Esse3CourseDao,  // Room DAO for offline cache
) {
    suspend fun getAcademicRecord(): Esse3AcademicRecord {
        return try {
            val record = api.career.getAcademicRecord()
            dao.insertCourses(record.courses.map { it.toEntity() })
            record
        } catch (e: Exception) {
            // Fall back to cached data
            val cached = dao.getAllCourses()
            if (cached.isNotEmpty()) {
                Esse3AcademicRecord(cached.map { it.toDomain() }, 0.0, 0.0)
            } else throw e
        }
    }
}
```

### ViewModel Pattern

```kotlin
@HiltViewModel
class AcademicRecordViewModel @Inject constructor(
    private val repository: Esse3CareerRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<Esse3AcademicRecord>>(UiState.Loading)
    val state = _state.asStateFlow()

    init { loadRecord() }

    private fun loadRecord() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                _state.value = UiState.Success(repository.getAcademicRecord())
            } catch (e: HtmlParsingException) {
                _state.value = UiState.Error("Unable to load record")
            } catch (e: Exception) {
                _state.value = UiState.Error("Connection error")
            }
        }
    }
}
```

### Session Management

The `Esse3Api` instance should be managed as a singleton via Hilt, created after successful login and destroyed on logout:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object Esse3ApiModule {
    @Provides
    @Singleton
    fun provideEsse3Api(storageManager: StorageManager): Esse3Api? {
        val cookies = storageManager.getEsse3SessionCookies()
        return if (cookies.isNotEmpty()) Esse3Api(cookies) else null
    }
}
```

## Known Limitations

1. **`getExamResults()` is not implemented** - The method exists but calls `TODO()`, which throws `NotImplementedError`. Do not use until implemented.

2. **Session expiration** - Sessions expire after ~30 minutes of inactivity. The scraper does not auto-refresh sessions. Implement session renewal or re-authentication in the repository layer.

3. **Rate limiting** - The ESSE3 web interface is not designed for rapid API-like access. Add reasonable delays between rapid sequential calls to avoid being throttled.

4. **Navigation flow requirements** - Many ESSE3 pages require visiting an entry point URL first (with `menu_opened_cod` parameter). The scraper handles this internally, but it means some methods make 2+ HTTP requests per call.

5. **Concurrent access** - The `HttpClient` in `Esse3Api` is shared across all sub-APIs. Ktor clients are thread-safe, but ESSE3's server-side session state may not handle truly concurrent requests well. Use `Mutex` or sequential coroutine execution for safety.

6. **Locale dependency** - All HTML parsing assumes Italian locale (field labels like "cognome", "nome", status strings like "Approvato"). The module will not work if ESSE3 language is changed to English.
