package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state.AppointmentDirectorySection

// The portal repeats the faculty grouping inside service names ("Didattica - Progest",
// "Psicologia (Gestione Carriere)"); the section header already says it, so strip it.
val AppointmentService.displayName: String
    get() = name
        .removePrefix("Didattica - ")
        .removePrefix("Gestione carriere - ")
        .replace(TrailingCareerSuffix, "")
        .trim()

private val TrailingCareerSuffix = Regex("""\s*\(Gestione [Cc]arriere\)$""")

val AppointmentService.durationLabel: String
    get() = "Appuntamento di ${durationSeconds / 60} min"

val AppointmentService.directoryIcon: ImageVector
    get() {
        val haystack = "$name ${group.orEmpty()}"
        return when {
            haystack.contains("Badge", ignoreCase = true) -> Icons.Outlined.Badge
            haystack.contains("Pergamene", ignoreCase = true) ||
                haystack.contains("Diplomi", ignoreCase = true) -> Icons.Outlined.WorkspacePremium
            haystack.contains("Dottorat", ignoreCase = true) -> Icons.Outlined.HistoryEdu
            haystack.contains("Specializzazione", ignoreCase = true) -> Icons.Outlined.MedicalServices
            haystack.contains("Diritto allo Studio", ignoreCase = true) -> Icons.Outlined.Savings
            haystack.contains("International", ignoreCase = true) ||
                haystack.contains("Erasmus", ignoreCase = true) -> Icons.Outlined.Public
            haystack.contains("Stage", ignoreCase = true) -> Icons.Outlined.Work
            haystack.contains("Orientamento", ignoreCase = true) -> Icons.Outlined.Explore
            haystack.contains("Carriere", ignoreCase = true) -> Icons.Outlined.School
            haystack.contains("Didattica", ignoreCase = true) -> Icons.AutoMirrored.Outlined.MenuBook
            else -> Icons.Outlined.SupportAgent
        }
    }

// The macro-section a portal group falls into, in a fixed order; unmatched groups fall into
// "Altri sportelli". Single source for both the directory headers and a booking's subtitle.
private val SectionCaptions = linkedMapOf(
    "Carriere studenti" to "Gestione della tua carriera",
    "Didattica" to "Sportelli dei corsi di studio",
    "Ritiri e consegne" to "Badge, pergamene e diplomi",
    "Altri sportelli" to "Orientamento, internazionale e altro",
)

fun sectionLabelOf(groupOrName: String): String = when {
    groupOrName.startsWith("Carriere Studenti", ignoreCase = true) -> "Carriere studenti"
    groupOrName.startsWith("Didattica", ignoreCase = true) -> "Didattica"
    groupOrName.startsWith("Ritiro", ignoreCase = true) -> "Ritiri e consegne"
    else -> "Altri sportelli"
}

fun List<AppointmentService>.toDirectorySections(): List<AppointmentDirectorySection> {
    val byLabel = groupBy { sectionLabelOf(it.group ?: it.name) }
    return SectionCaptions.entries.mapNotNull { (label, caption) ->
        byLabel[label]?.takeIf { it.isNotEmpty() }
            ?.let { AppointmentDirectorySection(label, caption, it.sortedBy { s -> s.displayName }) }
    }
}

// Check-in QRs arrive as "data:image/png;base64,…" data URLs.
fun decodeQrDataUrl(dataUrl: String): ImageBitmap? = runCatching {
    val encoded = dataUrl.substringAfter("base64,", missingDelimiterValue = "")
        .takeIf { it.isNotBlank() } ?: return null
    val bytes = Base64.decode(encoded, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
}.getOrNull()
