package it.attendance100.mybicocca.data.dto.esse3

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * An exam session available for booking.
 */
data class Esse3ExamSession(
    val id: Long,
    val courseCode: String,
    val courseName: String,
    val date: LocalDateTime?,
    val type: Esse3ExamType,
    val location: String?,
    val professor: String?,
    val closed: Boolean,
    val notes: String?
)

/**
 * Type of exam.
 */
enum class Esse3ExamType {
    WRITTEN,
    ORAL,
    PARTIAL,
    LAB,
    PROJECT,
    OTHER;

    companion object {
        fun fromString(value: String): Esse3ExamType {
            return when (value.lowercase()) {
                "scritto", "written" -> WRITTEN
                "orale", "oral" -> ORAL
                "parziale", "prova parziale", "partial" -> PARTIAL
                "laboratorio", "lab" -> LAB
                "progetto", "project" -> PROJECT
                else -> OTHER
            }
        }
    }
}

/**
 * An exam reservation made by the student.
 */
data class Esse3ExamReservation(
    val appId: Long,
    val adsceId: Long,
    val attDidEsaId: Long,
    val cdsEsaId: Long,
    val aaFreqId: Long,
    val date: LocalDateTime?,
    val courseCode: String,
    val courseName: String,
    val type: Esse3ExamType,
    val location: String?,
    val status: String?
) {
    /**
     * Returns query parameters for printing the reservation.
     */
    fun toPrintParams(): Map<String, String> = mapOf(
        "APP_ID" to appId.toString(),
        "ADSCE_ID" to adsceId.toString(),
        "ATT_DID_ESA_ID" to attDidEsaId.toString(),
        "CDS_ESA_ID" to cdsEsaId.toString(),
        "AA_FREQ_ID" to aaFreqId.toString(),
        "DATA_INIZIO_APP" to (date?.toLocalDate()?.toString() ?: "")
    )
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
