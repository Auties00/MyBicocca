package it.attendance100.mybicocca.data.dto.esse3

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * An exam session available for booking.
 */
data class Esse3ExamSession(
    val courseName: String,
    val examDate: LocalDate,
    val registrationStartDate: LocalDate,
    val registrationEndDate: LocalDate,
    val description: String,
    val examMode: Esse3ExamSessionMode,
    val academicYears: List<String>,
    val infoPath: String
)

/**
 * Complete exam session information.
 */
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
    val registrationNumber: Int?
)

/**
 * The exam session mode.
 */
enum class Esse3ExamSessionMode {
    IN_PERSON,
    REMOTE;

    companion object {
        fun fromString(value: String): Esse3ExamSessionMode? {
            return when (value.trim().lowercase()) {
                "p", "esame in presenza" -> IN_PERSON
                "d", "esame a distanza" -> REMOTE
                else -> null
            }
        }
    }
}

/**
 * Booked exam session.
 */
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
    val sessionId: String,
    val teachingActivityId: String
)


/**
 * Type of exam.
 */
sealed interface Esse3ExamType {
    data object Written : Esse3ExamType
    data object Oral : Esse3ExamType
    data object Partial : Esse3ExamType
    data class Other(val value: String) : Esse3ExamType

    companion object {
        fun fromString(value: String): Esse3ExamType {
            return when (value.trim().lowercase()) {
                "scritto", "scritto e orale" -> Written
                "orale" -> Oral
                "parziale", "prova parziale" -> Partial
                else -> Other(value)
            }
        }
    }
}

/**
 * Result of an exam.
 */
data class Esse3ExamResult(
    val courseCode: String,
    val courseName: String,
    val date: LocalDate?,
    val grade: Esse3Grade?,
    val status: Esse3ResultStatus,
    val professor: String?,
    val notes: String?
)

/**
 * Status of an exam result.
 */
enum class Esse3ResultStatus {
    PENDING,
    PUBLISHED,
    ACCEPTED,
    REJECTED,
    VERBALIZED;

    companion object {
        fun fromString(value: String): Esse3ResultStatus {
            return when (value.lowercase()) {
                "in attesa", "pending" -> PENDING
                "pubblicato", "published" -> PUBLISHED
                "accettato", "accepted" -> ACCEPTED
                "rifiutato", "rejected" -> REJECTED
                "verbalizzato", "verbalized" -> VERBALIZED
                else -> PENDING
            }
        }
    }
}


/**
 * Represents the reservation history for a specific course.
 */
data class Esse3CourseReservationHistory(
    val course: String,
    val entries: List<Esse3ReservationHistoryEntry>
)

/**
 * Represents a single entry in the reservation history log.
 */
data class Esse3ReservationHistoryEntry(
    val operationDateTime: LocalDateTime,
    val examDescription: String,
    val examDate: LocalDate?,
    val operation: Esse3ReservationOperation,
    val performedBy: String
)

/**
 * Represents an operation type in reservation history.
 */
enum class Esse3ReservationOperation {
    RESERVED,
    CANCELLED;

    companion object {
        fun fromString(text: String): Esse3ReservationOperation? {
            return when {
                text.contains("Effettuata", ignoreCase = true) -> RESERVED
                text.contains("Cancellata", ignoreCase = true) -> CANCELLED
                else -> null
            }
        }
    }
}