package it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.subscreen.yearDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.enrollment.AnnualEnrollment
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.component.EnrollmentBadgeChip
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.academicYearLabel
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.badges
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.courseYearLabel
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.statusLabel
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.toEnrollmentDateLabel
import it.attendance100.mybicocca.ui.screen.profile.subscreen.enrollments.ext.typeLabel

// Full breakdown for one academic year. Sections appear only when populated, so a clean
// career shows just the essentials while a fuori-corso / part-time / suspended year reveals
// the extra fields.
@Composable
fun EnrollmentDetailSheet(
    enrollment: AnnualEnrollment,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val badges = enrollment.badges()

    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp),
        ) {
            Column(modifier = Modifier.padding(start = 22.dp, top = 4.dp, end = 22.dp, bottom = 6.dp)) {
                Text(
                    text = "Iscrizione ${enrollment.academicYearLabel()}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.9).sp,
                    color = scheme.onSurface,
                )
                Text(
                    text = "${enrollment.courseYearLabel()} · ${enrollment.statusLabel()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (badges.isNotEmpty()) {
                    item {
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            badges.forEach { EnrollmentBadgeChip(it) }
                        }
                    }
                }

                item {
                    DetailSection(
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
                        DetailSection(
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
                        DetailSection(
                            title = "Sospensione",
                            rows = buildList {
                                susp.reasonCode?.let { add("Causale" to it) } ?: add("Stato" to "Sospesa")
                            },
                        )
                    }
                }

                if (enrollment.awaitingDegree || enrollment.degreeAwardDate != null) {
                    item {
                        DetailSection(
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
                    item { DetailSection(title = "Agevolazioni", rows = benefitRows) }
                }

                val disabilityRows = buildList {
                    enrollment.disabilityTypeDescription?.let { add("Tipologia" to it) }
                    enrollment.disabilityPercentage?.let {
                        add("Percentuale" to "${it.toInt()}%")
                    }
                }
                if (disabilityRows.isNotEmpty()) {
                    item { DetailSection(title = "Disabilità", rows = disabilityRows) }
                }

                item {
                    DetailSection(
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
                    item { DetailSection(title = "Sede", rows = locationRows) }
                }

                val dateRows = buildList {
                    enrollment.enrollmentDate?.let { add("Data iscrizione" to it.toEnrollmentDateLabel()) }
                    enrollment.insertionDate?.let { add("Inserita il" to it.toEnrollmentDateLabel()) }
                    enrollment.modificationDate?.let { add("Aggiornata il" to it.toEnrollmentDateLabel()) }
                }
                if (dateRows.isNotEmpty()) {
                    item { DetailSection(title = "Date", rows = dateRows) }
                }

                enrollment.enrollmentNote?.let { note ->
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(scheme.surfaceContainerHigh)
                                .padding(14.dp),
                        ) {
                            SectionTitle("Note")
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = note,
                                fontSize = 13.sp,
                                color = scheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    rows: List<Pair<String, String>>,
) {
    if (rows.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionTitle(title)
        rows.forEach { (label, value) -> DetailRow(label, value) }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.height(0.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f),
        )
    }
}
