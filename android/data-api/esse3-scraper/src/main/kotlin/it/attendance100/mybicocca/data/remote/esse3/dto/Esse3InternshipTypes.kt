package it.attendance100.mybicocca.data.remote.esse3.dto

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration

/**
 * Type of internship opportunity.
 */
sealed interface Esse3InternshipType {
    data object Curricular : Esse3InternshipType
    data object Extracurricular : Esse3InternshipType
    data object Cfu60 : Esse3InternshipType
    data object Tfa : Esse3InternshipType
    data class Other(val code: String, val description: String) : Esse3InternshipType

    companion object {
        fun fromCode(code: String): Esse3InternshipType {
            return when (code.trim().uppercase()) {
                "WTIR_C" -> Curricular
                "WTIR_E" -> Extracurricular
                "60CFU" -> Cfu60
                "TFA" -> Tfa
                else -> Other(code, code)
            }
        }

        fun fromDescription(description: String): Esse3InternshipType {
            val lower = description.trim().lowercase()
            return when {
                lower.contains("curricular") && !lower.contains("extra") -> Curricular
                lower.contains("extracurricular") || lower.contains("extra-curricular") -> Extracurricular
                lower.contains("60 cfu") || lower.contains("60cfu") -> Cfu60
                lower.contains("tfa") -> Tfa
                else -> Other("", description)
            }
        }
    }
}

/**
 * Status of an internship application.
 */
sealed interface Esse3ApplicationStatus {
    data object Submitted : Esse3ApplicationStatus
    data object UnderReview : Esse3ApplicationStatus
    data object Accepted : Esse3ApplicationStatus
    data object Rejected : Esse3ApplicationStatus
    data object Withdrawn : Esse3ApplicationStatus
    data class Other(val value: String) : Esse3ApplicationStatus

    companion object {
        fun fromString(value: String): Esse3ApplicationStatus {
            val lower = value.trim().lowercase()
            return when {
                lower == "inviata" || lower == "submitted" -> Submitted
                lower == "in valutazione" || lower == "under review" -> UnderReview
                lower == "accettata" || lower == "accepted" -> Accepted
                lower == "rifiutata" || lower == "rejected" -> Rejected
                lower == "ritirata" || lower == "withdrawn" -> Withdrawn
                else -> Other(value)
            }
        }
    }
}

/**
 * Status of an internship.
 */
sealed interface Esse3InternshipStatus {
    data object Active : Esse3InternshipStatus
    data object Completed : Esse3InternshipStatus
    data object Suspended : Esse3InternshipStatus
    data object Cancelled : Esse3InternshipStatus
    data class Other(val value: String) : Esse3InternshipStatus

    companion object {
        fun fromString(value: String): Esse3InternshipStatus {
            val lower = value.trim().lowercase()
            return when {
                lower == "attivo" || lower == "active" || lower == "in corso" -> Active
                lower == "completato" || lower == "completed" || lower == "concluso" -> Completed
                lower == "sospeso" || lower == "suspended" -> Suspended
                lower == "annullato" || lower == "cancelled" -> Cancelled
                else -> Other(value)
            }
        }
    }
}

/**
 * Career type for internship requirements.
 */
sealed interface Esse3CareerType {
    data object Bachelor : Esse3CareerType
    data object Master : Esse3CareerType
    data object SingleCycle : Esse3CareerType
    data class Other(val value: String) : Esse3CareerType

    companion object {
        fun fromString(value: String): Esse3CareerType {
            val lower = value.trim().lowercase()
            return when {
                lower.contains("triennio") || lower.contains("triennale") || lower.contains("bachelor") -> Bachelor
                lower.contains("biennio") || lower.contains("magistrale") || lower.contains("master") -> Master
                lower.contains("ciclo unico") || lower.contains("single cycle") -> SingleCycle
                else -> Other(value)
            }
        }

        fun parseList(value: String): List<Esse3CareerType> {
            return value.split(",", ";", ".")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { fromString(it) }
        }
    }
}

/**
 * An internship opportunity summary (list view).
 */
data class Esse3InternshipOpportunity(
    val id: Long,
    val title: String,
    val companyName: String,
    val type: Esse3InternshipType,
    val applicationEndDate: LocalDate,
    var isSaved: Boolean
)

/**
 * Detailed internship opportunity information.
 */
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
    val expectedDuration: Duration,
    val requirements: Esse3InternshipRequirements,
    val applicationEndDate: LocalDate,
    val isSaved: Boolean
)

/**
 * Internship requirements.
 */
data class Esse3InternshipRequirements(
    val reservedFor: String,
    val careerTypes: List<Esse3CareerType>,
    val languages: List<Esse3LanguageRequirement>
)

/**
 * Language requirement.
 */
data class Esse3LanguageRequirement(
    val language: String,
    val level: String
)

/**
 * Company summary (search results).
 */
data class Esse3Company(
    val id: Long,
    val name: String,
    val sector: String,
    val hasConvention: Boolean
)

/**
 * Company with full details.
 */
data class Esse3CompanyInformation(
    val id: Long,
    val name: String,
    val description: String,
    val logoUrl: String?,
    val locations: List<Esse3CompanyLocation>,
    val conventions: List<Esse3Convention>
)

/**
 * Company location/branch.
 */
data class Esse3CompanyLocation(
    val address: String,
    val type: String,
    val email: String?
)

/**
 * Convention between company and university.
 */
data class Esse3Convention(
    val name: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val durationYears: Int?,
    val autoRenewal: Boolean
)

/**
 * An internship application.
 */
data class Esse3InternshipApplication(
    val id: Long?,
    val opportunityId: Long,
    val opportunityTitle: String,
    val companyName: String,
    val type: String?,
    val status: Esse3ApplicationStatus,
    val applicationDate: LocalDateTime?
)

/**
 * An active or completed internship.
 */
data class Esse3Internship(
    val id: Long,
    val opportunityId: Long?,
    val title: String,
    val companyId: Long,
    val companyName: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val status: Esse3InternshipStatus
)

/**
 * Saved search for internship opportunities.
 */
data class Esse3SavedSearch(
    val id: Long,
    val description: String
)
