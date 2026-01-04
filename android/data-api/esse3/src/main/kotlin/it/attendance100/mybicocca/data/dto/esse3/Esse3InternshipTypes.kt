package it.attendance100.mybicocca.data.dto.esse3

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * An internship opportunity.
 */
data class Esse3InternshipOpportunity(
    val id: Long,
    val title: String,
    val company: Esse3Company,
    val type: Esse3InternshipType,
    val applicationStart: LocalDateTime?,
    val applicationEnd: LocalDateTime?,
    val description: String?,
    val requirements: String?,
    val location: String?,
    val duration: String?,
    val isSaved: Boolean = false
)

/**
 * Company offering internships.
 */
data class Esse3Company(
    val id: Long,
    val name: String,
    val description: String?,
    val sector: String?,
    val logoUrl: String?,
    val website: String?,
    val hasConvention: Boolean = false
)

/**
 * Type of internship.
 */
enum class Esse3InternshipType {
    CURRICULAR,
    EXTRACURRICULAR;

    companion object {
        fun fromString(value: String): Esse3InternshipType {
            return if (value.lowercase().contains("extracurriculare") ||
                value.lowercase().contains("extracurricular")
            ) {
                EXTRACURRICULAR
            } else {
                CURRICULAR
            }
        }
    }
}

/**
 * An internship application.
 */
data class Esse3InternshipApplication(
    val id: Long?,
    val opportunityId: Long,
    val opportunityTitle: String?,
    val companyName: String?,
    val status: Esse3ApplicationStatus,
    val applicationDate: LocalDateTime?,
    val notes: String?
)

/**
 * Status of an internship application.
 */
enum class Esse3ApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    companion object {
        fun fromString(value: String): Esse3ApplicationStatus {
            return when (value.lowercase()) {
                "inviata", "submitted" -> SUBMITTED
                "in valutazione", "under review" -> UNDER_REVIEW
                "accettata", "accepted" -> ACCEPTED
                "rifiutata", "rejected" -> REJECTED
                "ritirata", "withdrawn" -> WITHDRAWN
                else -> SUBMITTED
            }
        }
    }
}

/**
 * An active or completed internship.
 */
data class Esse3Internship(
    val id: Long,
    val opportunityId: Long?,
    val title: String,
    val company: Esse3Company,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val status: Esse3InternshipStatus,
    val tutor: String?,
    val credits: Int?
)

/**
 * Status of an internship.
 */
enum class Esse3InternshipStatus {
    ACTIVE,
    COMPLETED,
    SUSPENDED,
    CANCELLED;

    companion object {
        fun fromString(value: String): Esse3InternshipStatus {
            return when (value.lowercase()) {
                "attivo", "active", "in corso" -> ACTIVE
                "completato", "completed", "concluso" -> COMPLETED
                "sospeso", "suspended" -> SUSPENDED
                "annullato", "cancelled" -> CANCELLED
                else -> ACTIVE
            }
        }
    }
}

/**
 * Saved search for internship opportunities.
 */
data class Esse3SavedSearch(
    val id: Long,
    val description: String?,
    val searchText: String?,
    val filters: Map<String, String>
)

/**
 * Search filters for internship opportunities.
 */
data class Esse3InternshipSearchFilters(
    val searchText: String? = null,
    val type: Esse3InternshipType? = null,
    val sector: String? = null,
    val companyName: String? = null,
    val location: String? = null,
    val campaignId: Long? = null,
    val areaId: Long? = null,
    val advancedSearch: Boolean = false
) {
    fun toFormFields(): Map<String, String> = buildMap {
        searchText?.let { put("search_testo", it) }
        type?.let { put("tipoOpportunita", if (it == Esse3InternshipType.CURRICULAR) "C" else "E") }
        companyName?.let { put("ragione_sociale", it) }
        sector?.let { put("settore", it) }
        campaignId?.let { put("campagna_id", it.toString()) }
        areaId?.let { put("area_disc_id", it.toString()) }
        put("advanced_search", if (advancedSearch) "1" else "0")
    }
}
