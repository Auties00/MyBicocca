package it.attendance100.mybicocca.ui.screen.registry.subscreen.internships.ext

import it.attendance100.mybicocca.domain.model.internship.InternshipApplication
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationDetail
import it.attendance100.mybicocca.domain.model.internship.InternshipApplicationState
import it.attendance100.mybicocca.domain.model.internship.InternshipAttachment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Esse3 ships company names in all caps ("ACME CONSULTING S.R.L.").
val InternshipApplication.displayName: String
    get() = companyName?.toDisplayCase()
        ?: opportunityTitle
        ?: typeDescription
        ?: "Tirocinio"

val InternshipApplication.detailLine: String
    get() = buildList {
        typeDescription?.takeIf { it.isNotBlank() }?.let(::add)
        academicYearLabel?.takeIf { it.isNotBlank() }?.let(::add)
    }.joinToString(" · ").ifEmpty { stateDescription ?: "Domanda di tirocinio" }

val InternshipApplicationState.label: String
    get() = when (this) {
        InternshipApplicationState.Submitted -> "Presentata"
        InternshipApplicationState.Confirmed -> "Confermata"
        InternshipApplicationState.Started -> "In corso"
        InternshipApplicationState.Closed -> "Concluso"
        InternshipApplicationState.Cancelled -> "Annullata"
        InternshipApplicationState.Rejected -> "Rifiutata"
        InternshipApplicationState.NotAssigned -> "Non assegnata"
        InternshipApplicationState.Unknown -> "Sconosciuto"
    }

// Prefer the server-side label when Esse3 provides one; fall back to ours.
val InternshipApplication.stateLabel: String
    get() = stateDescription?.takeIf { it.isNotBlank() } ?: state.label

val InternshipApplicationDetail.durationLine: String?
    get() {
        val parts = buildList {
            durationMonths?.takeIf { it > 0 }?.let { add(if (it == 1L) "1 mese" else "$it mesi") }
            durationWeeks?.takeIf { it > 0 }?.let { add(if (it == 1L) "1 settimana" else "$it settimane") }
            durationDays?.takeIf { it > 0 }?.let { add(if (it == 1L) "1 giorno" else "$it giorni") }
            durationHours?.takeIf { it > 0 }?.let { add("$it ore") }
        }
        if (parts.isEmpty()) return null
        val weekly = weeklyInternshipDays?.takeIf { it > 0 }
            ?.let { if (it == 1L) " · 1 giorno a settimana" else " · $it giorni a settimana" }
            .orEmpty()
        return parts.joinToString(", ") + weekly
    }

val InternshipAttachment.displayTitle: String
    get() = title ?: fileName ?: "Allegato"

fun InternshipAttachment.sizeLabel(): String? {
    val bytes = sizeBytes?.takeIf { it > 0 } ?: return null
    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> "%.0f KB".format(Locale.ITALIAN, bytes / 1024.0)
        else -> "%.1f MB".format(Locale.ITALIAN, bytes / (1024.0 * 1024.0))
    }
}

private val SHORT_DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

fun LocalDate.toShortLabel(): String = format(SHORT_DATE)

fun formatDateRange(start: LocalDate?, end: LocalDate?): String? = when {
    start != null && end != null -> "${start.toShortLabel()} – ${end.toShortLabel()}"
    start != null -> "dal ${start.toShortLabel()}"
    end != null -> "fino al ${end.toShortLabel()}"
    else -> null
}

fun String.toDisplayCase(): String = trim()
    .split(Regex("\\s+"))
    .joinToString(" ") { word ->
        word.lowercase(Locale.ITALIAN).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
    }
