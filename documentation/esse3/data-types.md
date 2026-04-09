# Esse3 Data Types Reference

All DTO types used by the esse3-scraper module. These are the types you'll work with when implementing ESSE3 features in the app.

**Package:** `it.attendance100.mybicocca.data.dto.esse3`

---

## Profile Types

**Source:** `Esse3StudentTypes.kt`

### `Esse3PersonalData`
```kotlin
data class Esse3PersonalData(
    val name: String,
    val surname: String,
    val sex: Esse3Sex,             // MALE / FEMALE
    val birthDate: LocalDate,
    val citizenship: String,       // e.g., "ITALIANA"
    val birthCountry: String,
    val birthProvince: String,
    val birthCity: String,
    val fiscalcode: String         // Codice Fiscale
)
```

### `Esse3Sex`
```kotlin
enum class Esse3Sex(val code: String) {
    MALE("Maschio"),
    FEMALE("Femmina")
}
```

### `Esse3ResidenceAddress`
```kotlin
data class Esse3ResidenceAddress(
    val country: String,
    val province: String,
    val city: String,
    val zipCode: String,
    val district: String,          // Frazione
    val street: String,
    val houseNumber: String,
    val phone: String,
    val coincidesWithDomicile: Boolean
)
```

### `Esse3ResidenceAddressOptions`
Dropdown options for the address form:
```kotlin
data class Esse3ResidenceAddressOptions(
    val countries: List<String>,
    val cities: List<String>,
    val provinces: List<String>
)
```

### `Esse3ContactInfo`
```kotlin
data class Esse3ContactInfo(
    val documentDelivery: Esse3AddressType,  // RESIDENCE or DOMICILE
    val taxDelivery: Esse3AddressType,
    val email: String,
    val fax: String,
    val mobile: String,                      // Full international format (e.g., "+393401234567")
    val privacyConsent: Boolean
)
```

### `Esse3AddressType`
```kotlin
enum class Esse3AddressType(val code: String) {
    RESIDENCE("R"),
    DOMICILE("D")
}
```

---

## Career / Academic Types

**Source:** `Esse3CareerTypes.kt`

### `Esse3AcademicRecord`
The full "libretto" (academic record book):
```kotlin
data class Esse3AcademicRecord(
    val courses: List<Esse3Course>,
    val unweightedGpa: Double,     // Media Aritmetica
    val weightedGpa: Double        // Media Ponderata (weighted by credits)
)
```

### `Esse3Course`
A single course entry in the libretto:
```kotlin
data class Esse3Course(
    val code: String,              // e.g., "E3101Q116"
    val name: String,              // e.g., "ALGORITMI E STRUTTURE DATI"
    val urlPath: String,           // Path for getCourseInfo()
    val year: Int,                 // Year of course (1, 2, 3)
    val credits: Int,              // CFU
    val academicYear: String,      // e.g., "2024/2025"
    val grade: Esse3Grade?,        // null if not yet graded
    val examDate: LocalDate?,      // null if not yet taken
    val examAttemptsUrlPath: String? // Path for getCourseExamAttempts(), null if no attempts
)
```

### `Esse3Grade` (sealed class)
```kotlin
sealed class Esse3Grade {
    data class Numeric(val value: Int, val cumLaude: Boolean)  // 18-30, toString: "30L" or "24"
    data object Passed      // IDO (Idoneo) - pass without numeric grade
    data object Approved    // APPR (Approvato)
    data object Absent      // ASS (Assente)
    data object Failed      // INS (Insufficiente)
    data object Withdrawn   // RIT (Ritirato)
}

// Parsing: Esse3Grade.parse("30L") -> Numeric(30, true)
//          Esse3Grade.parse("IDO") -> Passed
//          Esse3Grade.parse("24")  -> Numeric(24, false)
```

### `Esse3CourseDetails`
Extended course information from the detail page:
```kotlin
data class Esse3CourseDetails(
    val code: String,
    val name: String,
    val courseCode: String,         // Degree program code
    val courseName: String,        // Degree program name
    val degreeCode: String,        // "L-31" etc.
    val degreeDescription: String, // "Classe delle Lauree in..."
    val year: Int,
    val status: Esse3CourseStatus,
    val examDate: LocalDate?,
    val grade: Esse3Grade?,
    val notes: String,
    val units: List<Esse3CourseUnit>
)
```

### `Esse3CourseStatus` (sealed class)
```kotlin
sealed class Esse3CourseStatus {
    data object NotAttended
    data class Attended(val year: String)                              // e.g., "2024/2025"
    data class Passed(val year: String, val attendedYear: String)      // Both years tracked
}
```

### `Esse3CourseUnit`
A didactic unit (module) within a course:
```kotlin
data class Esse3CourseUnit(
    val name: String,
    val activityType: String,      // e.g., "Lezione"
    val formationType: String,     // e.g., "Base"
    val sector: String,            // e.g., "INF/01"
    val credits: Int,
    val duration: Int?             // Hours, nullable
)
```

### `Esse3ExamAttempt`
```kotlin
data class Esse3ExamAttempt(
    val examDate: LocalDate?,
    val examType: String,
    val outcome: Esse3ExamAttemptOutcome,
    val verbalizationDate: LocalDate?
)
```

### `Esse3ExamAttemptOutcome` (sealed class)
```kotlin
sealed class Esse3ExamAttemptOutcome {
    data class Passed(val grade: Esse3Grade)
    data object Failed
    data object Absent
    data object Withdrawn
    data object Booked
}
```

### `Esse3StudyPlan`
```kotlin
data class Esse3StudyPlan(
    val status: Esse3PlanStatus,   // APPROVED, PENDING, REJECTED, DRAFT
    val type: String,
    val lastModified: LocalDate,
    val offerYear: Int,
    val regulationYear: Int,
    val courses: List<Esse3PlannedCourse>
)
```

### `Esse3PlannedCourse`
```kotlin
data class Esse3PlannedCourse(
    val code: String,
    val description: String,
    val year: Int,
    val university: String
)
```

### `Esse3PlanStatus`
```kotlin
enum class Esse3PlanStatus {
    APPROVED,   // "APPROVATO"
    PENDING,    // "IN ATTESA"
    REJECTED,   // "RIFIUTATO"
    DRAFT       // "BOZZA"
}
```

---

## Exam Types

**Source:** `Esse3ExamTypes.kt`

### `Esse3ExamSession`
An exam call available for booking:
```kotlin
data class Esse3ExamSession(
    val courseName: String,
    val examDate: LocalDate,
    val registrationStartDate: LocalDate,
    val registrationEndDate: LocalDate,
    val description: String,
    val examMode: Esse3ExamSessionMode,
    val academicYears: List<String>,
    val infoPath: String                    // URL path for detail/booking
)
```

### `Esse3ExamSessionInformation`
Full exam session details:
```kotlin
data class Esse3ExamSessionInformation(
    val examSession: Esse3ExamSession,
    val teachingActivity: String,
    val description: String,
    val sessions: List<String>,
    val type: Esse3ExamType,
    val verbalization: String,
    val teachers: List<String>,
    val notes: String?,
    val datetime: LocalDateTime,
    val building: String,
    val room: String,
    val registrationNumber: Int?            // Current registration count
)
```

### `Esse3ExamReservation`
A booked exam:
```kotlin
data class Esse3ExamReservation(
    val teachingActivity: String,
    val reservationNumber: Int,
    val maxReservationsCount: Int,
    val examMode: Esse3ExamSessionMode,
    val description: String,
    val type: Esse3ExamType,
    val teachers: List<String>,
    val notes: String?,
    val datetime: LocalDateTime,
    val building: String,
    val room: String,
    val sessionId: String,                  // Used for cancel/print operations
    val teachingActivityId: String          // Used for cancel/print operations
)
```

### `Esse3ExamSessionMode`
```kotlin
enum class Esse3ExamSessionMode {
    IN_PERSON,  // "P" or "Esame in presenza"
    REMOTE      // "D" or "Esame a distanza"
}
```

### `Esse3ExamType` (sealed interface)
```kotlin
sealed interface Esse3ExamType {
    data object Written     // "Scritto" or "Scritto e orale"
    data object Oral        // "Orale"
    data object Partial     // "Parziale" or "Prova parziale"
    data class Other(val value: String)
}
```

### `Esse3ExamResult`
```kotlin
data class Esse3ExamResult(
    val courseCode: String,
    val courseName: String,
    val date: LocalDate?,
    val grade: Esse3Grade?,
    val status: Esse3ResultStatus,
    val professor: String?,
    val notes: String?
)
```

### `Esse3ResultStatus`
```kotlin
enum class Esse3ResultStatus {
    PENDING,      // "In attesa"
    PUBLISHED,    // "Pubblicato"
    ACCEPTED,     // "Accettato"
    REJECTED,     // "Rifiutato"
    VERBALIZED    // "Verbalizzato"
}
```

### `Esse3CourseReservationHistory` / `Esse3ReservationHistoryEntry`
```kotlin
data class Esse3CourseReservationHistory(
    val course: String,
    val entries: List<Esse3ReservationHistoryEntry>
)

data class Esse3ReservationHistoryEntry(
    val operationDateTime: LocalDateTime,
    val examDescription: String,
    val examDate: LocalDate?,
    val operation: Esse3ReservationOperation,  // RESERVED or CANCELLED
    val performedBy: String
)
```

---

## Tax / Payment Types

**Source:** `Esse3TaxesTypes.kt`

### `Esse3TaxBill`
```kotlin
data class Esse3TaxBill(
    val id: Long,                           // Unique bill ID
    val invoiceNumber: String,              // Displayed invoice number
    val description: String,                // e.g., "Matricola 909697 - Corso di Laurea - INFORMATICA - Rata: 1 di 3"
    val dueDate: LocalDate?,
    val amount: BigDecimal,                 // Total amount in EUR
    val paymentStatus: Esse3PaymentStatus,
    val pagoPaAvailable: Boolean
)
```

### `Esse3PaymentStatus` (sealed interface)
```kotlin
sealed interface Esse3PaymentStatus {
    data object PaidConfirmed   // "Pagato" + "confermato"
    data object PaidPending     // "Pagato" (not yet confirmed)
    data object Unpaid          // "Non pagato" or "Da pagare"
    data object Overdue         // "Scaduto" or contains "mora"
    data object Cancelled       // "Annullat..." or "Storn..."
    data class Other(val value: String)
}
```

### `Esse3TaxBillDetail`
```kotlin
data class Esse3TaxBillDetail(
    val bill: Esse3TaxBill,
    val items: List<Esse3TaxBillItem>,
    val paymentMethod: Esse3PaymentMethod,
    val pagoPAInfo: Esse3PagoPAInfo?
)
```

### `Esse3TaxBillItem`
```kotlin
data class Esse3TaxBillItem(
    val academicYear: String,       // e.g., "2024/2025"
    val installment: String,        // e.g., "3 di 3"
    val description: String?,       // e.g., "Contributi Universitari"
    val amount: BigDecimal
)
```

### `Esse3PaymentMethod` (sealed interface)
```kotlin
sealed interface Esse3PaymentMethod {
    data object PagoPA          // "pagopa"
    data object BankTransfer    // "MAV", "RAV", or "bonifico"
    data class Other(val value: String)
}
```

### `Esse3PagoPAInfo`
```kotlin
data class Esse3PagoPAInfo(
    val noticeCode: String,                // Codice Avviso
    val id: String?,                       // IUV (Identificativo Univoco Versamento)
    val requestId: String?,                // RPT ID (needed for receipt download)
    val date: LocalDate?,                  // Payment date
    val requestStatus: Esse3RptStatus?,
    val transactionOutcome: String?        // Detailed outcome message
)
```

### `Esse3RptStatus` (sealed interface)
```kotlin
sealed interface Esse3RptStatus {
    data object Accepted    // "accettata" or "successo"
    data object Pending     // "in attesa"
    data object Rejected    // "rifiutata"
    data class Error(val message: String)   // "errore"
    data class Other(val value: String)
}
```

---

## Internship Types

**Source:** `Esse3InternshipTypes.kt`

### `Esse3InternshipOpportunity`
```kotlin
data class Esse3InternshipOpportunity(
    val id: Long,
    val title: String,
    val companyName: String,
    val type: Esse3InternshipType,
    val applicationEndDate: LocalDate,
    var isSaved: Boolean                    // Mutable - updated by save/unsave
)
```

### `Esse3InternshipOpportunityDetail`
```kotlin
data class Esse3InternshipOpportunityDetail(
    val id: Long,
    val title: String,
    val type: Esse3InternshipType,
    val companyName: String,
    val companyDescription: String?,
    val description: String,
    val trainingObjectives: String,
    val location: String,
    val functionalArea: String,
    val benefits: String?,
    val expectedStartDate: LocalDate,
    val expectedDuration: Duration,         // kotlin.time.Duration
    val requirements: Esse3InternshipRequirements,
    val applicationEndDate: LocalDate,
    val isSaved: Boolean
)
```

### `Esse3InternshipType` (sealed interface)
```kotlin
sealed interface Esse3InternshipType {
    data object Curricular          // WTIR_C - Tirocinio curriculare
    data object Extracurricular     // WTIR_E - Tirocinio extracurriculare
    data object Cfu60               // 60CFU
    data object Tfa                 // TFA
    data class Other(val code: String, val description: String)
}
```

### `Esse3InternshipRequirements`
```kotlin
data class Esse3InternshipRequirements(
    val reservedFor: String,
    val careerTypes: List<Esse3CareerType>,
    val languages: List<Esse3LanguageRequirement>
)
```

### `Esse3CareerType` (sealed interface)
```kotlin
sealed interface Esse3CareerType {
    data object Bachelor      // "triennale" / "triennio"
    data object Master        // "magistrale" / "biennio"
    data object SingleCycle   // "ciclo unico"
    data class Other(val value: String)
}
```

### `Esse3LanguageRequirement`
```kotlin
data class Esse3LanguageRequirement(
    val language: String,
    val level: String       // e.g., "B2"
)
```

### `Esse3Company` / `Esse3CompanyInformation`
```kotlin
data class Esse3Company(
    val id: Long,
    val name: String,
    val sector: String,
    val hasConvention: Boolean
)

data class Esse3CompanyInformation(
    val id: Long,
    val name: String,
    val description: String,
    val logoUrl: String?,
    val locations: List<Esse3CompanyLocation>,
    val conventions: List<Esse3Convention>
)
```

### `Esse3CompanyLocation` / `Esse3Convention`
```kotlin
data class Esse3CompanyLocation(
    val address: String,
    val type: String,
    val email: String?
)

data class Esse3Convention(
    val name: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val durationYears: Int?,
    val autoRenewal: Boolean
)
```

### `Esse3InternshipApplication`
```kotlin
data class Esse3InternshipApplication(
    val id: Long?,
    val opportunityId: Long,
    val opportunityTitle: String,
    val companyName: String,
    val type: String?,
    val status: Esse3ApplicationStatus,     // Submitted/UnderReview/Accepted/Rejected/Withdrawn
    val applicationDate: LocalDateTime?
)
```

### `Esse3ApplicationStatus` (sealed interface)
```kotlin
sealed interface Esse3ApplicationStatus {
    data object Submitted       // "Inviata"
    data object UnderReview     // "In valutazione"
    data object Accepted        // "Accettata"
    data object Rejected        // "Rifiutata"
    data object Withdrawn       // "Ritirata"
    data class Other(val value: String)
}
```

### `Esse3Internship`
```kotlin
data class Esse3Internship(
    val id: Long,
    val opportunityId: Long?,
    val title: String,
    val companyId: Long,
    val companyName: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val status: Esse3InternshipStatus       // Active/Completed/Suspended/Cancelled
)
```

### `Esse3SavedSearch`
```kotlin
data class Esse3SavedSearch(
    val id: Long,
    val description: String
)
```

---

## Questionnaire Types

**Source:** `Esse3QuestionnaireTypes.kt`

### `Esse3EvaluationCourse`
```kotlin
data class Esse3EvaluationCourse(
    val courseCode: String,
    val courseName: String,
    val year: Int,
    val credits: Int,
    val academicYear: String,
    val status: Esse3EvaluationCourseStatus
)
```

### `Esse3EvaluationCourseStatus` (sealed interface)
```kotlin
sealed interface Esse3EvaluationCourseStatus {
    data object NotAvailable                        // No questionnaire available
    data class Pending(val activityId: Long)         // Available, not yet completed
    data class Completed(val activityId: Long)       // Already submitted
}
```

### `Esse3EvaluationPartition`
```kotlin
data class Esse3EvaluationPartition(
    val unitName: String,
    val teacher: String,
    val activityType: String,
    val partition: String,
    val status: Esse3EvaluationPartitionStatus
)
```

### `Esse3EvaluationPartitionStatus` (sealed interface)
```kotlin
sealed interface Esse3EvaluationPartitionStatus {
    data object NotAvailable
    data class Pending(val questionnaireUrl: String)
    data object Completed
}
```

### `Esse3QuestionnairePage`
```kotlin
data class Esse3QuestionnairePage(
    val navigation: Esse3QuestionnaireNavigation,
    val title: String?,
    val sectionTitle: String?,
    val questions: List<Esse3Question>,
    val requiredQuestionIds: List<Long>,
    val canGoBack: Boolean,
    val canGoForward: Boolean
)
```

### `Esse3QuestionnaireNavigation`
Internal navigation state. Has `toFormFields(): Map<String, String>` for form submission.
```kotlin
data class Esse3QuestionnaireNavigation(
    val questId: Long,
    val questCompId: Long,
    val pageId: Long,
    val userCompId: String,
    val questConfigId: String,
    val redirectUrl: String,
    val eventoCompCod: String,
    val adsceId: Long
)
```

### `Esse3Question` (sealed interface)
```kotlin
sealed interface Esse3Question {
    val id: Long
    val fieldName: String      // Form field name for submission
    val text: String           // Question text
    val required: Boolean

    data class FreeText(...)        // Text input/textarea, optional maxLength
    data class SingleChoice(...)    // Radio/dropdown with List<Esse3QuestionOption>
    data class MultipleChoice(...)  // Checkboxes with List<Esse3QuestionOption>
    data class Rating(...)          // Numeric scale with minValue/maxValue
}
```

### `Esse3QuestionOption`
```kotlin
data class Esse3QuestionOption(
    val value: String,     // Value to submit
    val text: String       // Display text
)
```

### `Esse3QuestionnaireSubmitResult` (sealed interface)
```kotlin
sealed interface Esse3QuestionnaireSubmitResult {
    data class NextPage(val page: Esse3QuestionnairePage)
    data object Completed
    data class ValidationError(val message: String)
}
```
