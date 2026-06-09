package it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExamDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.ui.component.card.DetailFactCard
import it.attendance100.mybicocca.ui.component.exam.ExamGradeBadge
import it.attendance100.mybicocca.ui.component.exam.shortLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component.ExamCallChips
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.creditsLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.examVariantLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.locationLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.partialLabel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.ExamDocument
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

// A sat appello, paged inside the prenotazioni sheet. Mirror of the active detail but read
// only: the grade leads as an expressive polygon when the outcome is published, the facts
// follow, and the documents are the booking slip and the attendance certificate — no cancel
// or directions, since the exam is over.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PastBookedExamDetailPage(
    booking: BookedExam,
    downloadingDocument: ExamDocument?,
    onDownloadSlip: (BookedExam) -> Unit,
    onDownloadCertificate: (BookedExam) -> Unit,
) {
    val location = remember(booking.classroomDescription, booking.buildingDescription) {
        booking.locationLabel()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 640.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        if (booking.outcomePublished && booking.grade != ExamGrade.Unknown) {
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ExamGradeBadge(
                    grade = booking.grade,
                    size = 112.dp,
                    style = if (booking.grade.shortLabel().length <= 3) {
                        MaterialTheme.typography.displaySmallEmphasized
                    } else {
                        MaterialTheme.typography.headlineMediumEmphasized
                    },
                    showDenominator = true,
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        ExamCallChips(
            examType = booking.examType,
            variantLabel = booking.examVariantLabel(),
            partialLabel = booking.partialLabel(),
            creditsLabel = booking.creditsLabel(),
        )

        Spacer(Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            booking.examDateTime?.let { dt ->
                DetailFactCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "SOSTENUTO IL",
                    value = buildString {
                        append(
                            dt.toLocalDate().format(FullDateFormat)
                                .replaceFirstChar { it.titlecase(Locale.ITALIAN) },
                        )
                        append(" · ore ")
                        append(dt.toLocalTime().format(TimeFormat))
                    },
                )
            }
            location?.let {
                DetailFactCard(
                    icon = Icons.Outlined.LocationOn,
                    label = "LUOGO",
                    value = it,
                )
            }
            booking.examModeDescription?.let {
                DetailFactCard(
                    icon = Icons.Outlined.School,
                    label = "MODALITÀ",
                    value = it,
                )
            }
            booking.publishedNote?.let {
                DetailFactCard(
                    icon = Icons.AutoMirrored.Outlined.Notes,
                    label = "NOTA DEL DOCENTE",
                    value = it,
                )
            }
            booking.studentNote?.let {
                DetailFactCard(
                    icon = Icons.AutoMirrored.Outlined.Notes,
                    label = "NOTA",
                    value = it,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        PastDocumentsRow(
            downloading = downloadingDocument,
            onSlip = { onDownloadSlip(booking) },
            onCertificate = { onDownloadCertificate(booking) },
        )
    }
}

// Two connected tonal buttons: the booking slip always works; the attendance certificate
// 422s until the outcome is published — handled upstream as a snackbar, so both stay enabled.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PastDocumentsRow(
    downloading: ExamDocument?,
    onSlip: () -> Unit,
    onCertificate: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val busy = downloading != null
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = onSlip,
            enabled = !busy,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
        ) {
            if (downloading == ExamDocument.BookingSlip) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = scheme.onSecondaryContainer,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Statino", fontWeight = FontWeight.SemiBold)
            }
        }
        FilledTonalButton(
            onClick = onCertificate,
            enabled = !busy,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
        ) {
            if (downloading == ExamDocument.PresenceCertificate) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = scheme.onSecondaryContainer,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Attestato", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
