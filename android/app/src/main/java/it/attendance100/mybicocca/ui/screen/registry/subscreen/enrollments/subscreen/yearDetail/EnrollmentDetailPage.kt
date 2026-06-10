package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.subscreen.yearDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessible
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.component.EnrollmentBadgeChip
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.badges
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.toEnrollmentDateLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments.ext.typeLabel

/**
 * Full breakdown for one academic year, as a headerless page inside the iscrizioni sheet
 * pager: the sheet's morphing header carries the year and status line. Badge chips lead
 * when present, then each populated topic is a grouped tonal block led by an icon chip,
 * with the fields as divider-separated key/value rows — sections appear only when they
 * have content, so a clean career shows just the essentials while a fuori-corso /
 * part-time / suspended year reveals the extra fields.
 */
@Composable
fun EnrollmentDetailPage(
    enrollment: AnnualEnrollment,
    modifier: Modifier = Modifier,
) {
    val badges = enrollment.badges()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (badges.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    badges.forEach { EnrollmentBadgeChip(it) }
                }
            }
        }

        item {
            DetailGroup(
                icon = Icons.Outlined.HowToReg,
                title = "Iscrizione",
                rows = buildList {
                    add("Tipo iscrizione" to enrollment.typeLabel())
                    enrollment.studentTypeDescription?.let { add("Tipologia studente" to it) }
                    if (enrollment.outOfCourseYears > 0) {
                        add("Anni fuori corso" to enrollment.outOfCourseYears.toString())
                    }
                    if (enrollment.conditional) add("Iscrizione" to "Condizionata")
                    if (enrollment.reconstructed) add("Origine dato" to "Ricostruita")
                    enrollment.statusReasonCode?.let { add("Motivo stato" to it) }
                },
            )
        }

        enrollment.partTime?.let { pt ->
            item {
                DetailGroup(
                    icon = Icons.Outlined.Schedule,
                    title = "Part-time",
                    rows = buildList {
                        pt.credits?.let { add("CFU previsti" to "$it CFU") }
                        pt.extraCredits?.let { add("CFU extra" to "$it CFU") }
                        add("Modifiche" to if (pt.locked) "Bloccate" else "Consentite")
                    },
                )
            }
        }

        enrollment.suspension?.let { susp ->
            item {
                DetailGroup(
                    icon = Icons.Outlined.PauseCircle,
                    title = "Sospensione",
                    rows = buildList {
                        susp.reasonCode?.let { add("Causale" to it) } ?: add("Stato" to "Sospesa")
                    },
                )
            }
        }

        if (enrollment.awaitingDegree || enrollment.degreeAwardDate != null) {
            item {
                DetailGroup(
                    icon = Icons.Outlined.School,
                    title = "Laurea",
                    rows = buildList {
                        if (enrollment.awaitingDegree) add("Stato" to "In attesa di laurea")
                        enrollment.degreeAwardDate?.let {
                            add("Data attesa laurea" to it.toEnrollmentDateLabel())
                        }
                    },
                )
            }
        }

        val benefitRows = buildList {
            enrollment.exemptionDescription?.let { add("Esonero" to it) }
            enrollment.incomeBandId?.let { add("Fascia di reddito" to it.toString()) }
            enrollment.canteenBandId?.let { add("Fascia mensa" to it.toString()) }
            enrollment.meritBandId?.let { add("Fascia di merito" to it.toString()) }
            enrollment.meritNote?.let { add("Nota merito" to it) }
        }
        if (benefitRows.isNotEmpty()) {
            item {
                DetailGroup(icon = Icons.Outlined.Savings, title = "Agevolazioni", rows = benefitRows)
            }
        }

        val disabilityRows = buildList {
            enrollment.disabilityTypeDescription?.let { add("Tipologia" to it) }
            enrollment.disabilityPercentage?.let {
                add("Percentuale" to "${it.toInt()}%")
            }
        }
        if (disabilityRows.isNotEmpty()) {
            item {
                DetailGroup(icon = Icons.Outlined.Accessible, title = "Disabilità", rows = disabilityRows)
            }
        }

        item {
            DetailGroup(
                icon = Icons.Outlined.MenuBook,
                title = "Corso di studio",
                rows = buildList {
                    enrollment.courseDescription?.let { add("Corso" to it) }
                    enrollment.courseTypeDescription?.let { add("Tipo corso" to it) }
                    enrollment.degreeClassDescription?.let { add("Classe" to it) }
                    enrollment.degreeClassCode?.let { add("Codice classe" to it) }
                    enrollment.addressDescription
                        ?.takeIf { !it.equals("PERCORSO COMUNE", ignoreCase = true) }
                        ?.let { add("Percorso" to it) }
                    enrollment.orientationDescription?.let { add("Orientamento" to it) }
                    enrollment.studyOrderDescription?.let { add("Ordinamento" to it) }
                    enrollment.minimumCredits?.let { add("CFU per il titolo" to "$it CFU") }
                    enrollment.courseDuration?.let { add("Durata" to "$it anni") }
                    enrollment.teachingLanguage?.let { add("Lingua" to it) }
                    enrollment.regulationCode?.let { add("Normativa" to it) }
                },
            )
        }

        val locationRows = buildList {
            enrollment.universityDescription?.let { add("Ateneo" to it) }
            enrollment.siteDescription?.let { add("Sede" to it) }
        }
        if (locationRows.isNotEmpty()) {
            item { DetailGroup(icon = Icons.Outlined.Place, title = "Sede", rows = locationRows) }
        }

        val dateRows = buildList {
            enrollment.enrollmentDate?.let { add("Data iscrizione" to it.toEnrollmentDateLabel()) }
            enrollment.insertionDate?.let { add("Inserita il" to it.toEnrollmentDateLabel()) }
            enrollment.modificationDate?.let { add("Aggiornata il" to it.toEnrollmentDateLabel()) }
        }
        if (dateRows.isNotEmpty()) {
            item { DetailGroup(icon = Icons.Outlined.Event, title = "Date", rows = dateRows) }
        }

        enrollment.enrollmentNote?.let { note ->
            item { NoteGroup(note = note) }
        }
    }
}

/**
 * One topic: an icon-chip header above a single tonal container holding the fields as
 * divider-separated rows. The header sits outside the container so the eye groups the
 * rows under it without the title competing with the surface fill.
 */
@Composable
private fun DetailGroup(
    icon: ImageVector,
    title: String,
    rows: List<Pair<String, String>>,
) {
    if (rows.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        GroupHeader(icon = icon, title = title)
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                rows.forEachIndexed { index, (label, value) ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                    KeyValueRow(label = label, value = value)
                }
            }
        }
    }
}

@Composable
private fun NoteGroup(note: String) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        GroupHeader(icon = Icons.Outlined.Notes, title = "Note")
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun GroupHeader(icon: ImageVector, title: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f),
        )
    }
}
