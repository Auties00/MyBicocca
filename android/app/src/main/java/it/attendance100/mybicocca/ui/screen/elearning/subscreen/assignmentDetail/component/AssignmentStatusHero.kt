package it.attendance100.mybicocca.ui.screen.elearning.subscreen.assignmentDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// The page's emotional anchor: one card that answers "dove sono con questo compito?"
// at a glance, tinted by state — open (tertiary), handed in (primary), overdue (error).
@Composable
fun AssignmentStatusHero(
    assignment: Assignment,
    now: Instant,
    onOpenPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val status = assignment.submissionStatus) {
        is SubmissionStatus.Graded -> GradedHero(status, modifier)
        is SubmissionStatus.Submitted -> SubmittedHero(assignment, status, modifier)
        is SubmissionStatus.Draft -> DraftHero(assignment, now, onOpenPage, modifier)
        SubmissionStatus.NotSubmitted -> {
            val due = assignment.dueDate
            if (due != null && due.isBefore(now)) {
                OverdueHero(assignment, now, onOpenPage, modifier)
            } else {
                OpenHero(assignment, now, onOpenPage, modifier)
            }
        }
    }
}

@Composable
private fun OpenHero(
    assignment: Assignment,
    now: Instant,
    onOpenPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val due = assignment.dueDate
    val opensAt = assignment.allowSubmissionsFrom?.takeIf { it.isAfter(now) }
    val opensLater = opensAt != null
    val eyebrow = when {
        due != null -> "✦ ${formatCountdown(now, due)}"
        else -> "✦ SENZA SCADENZA"
    }
    val caption = when {
        opensAt != null -> "La consegna apre ${FullDateFmt.format(opensAt)} alle ${TimeFmt.format(opensAt)}"
        due != null -> "Scade ${FullDateFmt.format(due)} alle ${TimeFmt.format(due)}"
        else -> "Puoi consegnare in qualsiasi momento"
    }
    HeroCard(
        container = scheme.tertiaryContainer,
        accent = scheme.tertiary,
        onContainer = scheme.onTertiaryContainer,
        decorShape = OrganicShapes.Puffy,
        eyebrow = eyebrow,
        headline = "Da consegnare",
        caption = caption,
        cta = if (assignment.pageUrl != null && !opensLater) "Consegna sul sito" else null,
        onCta = onOpenPage,
        modifier = modifier,
    )
}

@Composable
private fun DraftHero(
    assignment: Assignment,
    now: Instant,
    onOpenPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val due = assignment.dueDate
    val eyebrow = if (due != null && due.isAfter(now)) "✦ ${formatCountdown(now, due)}" else "✦ BOZZA"
    HeroCard(
        container = scheme.secondaryContainer,
        accent = scheme.secondary,
        onContainer = scheme.onSecondaryContainer,
        decorShape = OrganicShapes.SmoothCookie6,
        eyebrow = eyebrow,
        headline = "Bozza salvata",
        caption = "La bozza non vale come consegna: completa l'invio sul sito",
        cta = assignment.pageUrl?.let { "Completa consegna" },
        onCta = onOpenPage,
        modifier = modifier,
    )
}

@Composable
private fun OverdueHero(
    assignment: Assignment,
    now: Instant,
    onOpenPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val due = assignment.dueDate ?: return
    val lateUntil = assignment.cutoffDate?.takeIf { it.isAfter(now) }
    val lateWindowOpen = lateUntil != null
    val caption = buildString {
        append("Scaduto ${FullDateFmt.format(due)} alle ${TimeFmt.format(due)}")
        if (lateUntil != null) {
            append(" · consegne in ritardo fino al ${FullDateFmt.format(lateUntil)}")
        }
    }
    HeroCard(
        container = scheme.errorContainer,
        accent = scheme.error,
        onContainer = scheme.onErrorContainer,
        decorShape = OrganicShapes.Burst,
        eyebrow = "✸ SCADUTO",
        headline = "Non consegnato",
        caption = caption,
        cta = if (lateWindowOpen && assignment.pageUrl != null) "Consegna in ritardo" else null,
        onCta = onOpenPage,
        modifier = modifier,
    )
}

@Composable
private fun SubmittedHero(
    assignment: Assignment,
    status: SubmissionStatus.Submitted,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val late = assignment.dueDate != null && status.submittedAt?.isAfter(assignment.dueDate) == true
    val caption = buildString {
        val at = status.submittedAt
        if (at != null) {
            append("Inviato ${FullDateFmt.format(at)} alle ${TimeFmt.format(at)}")
        } else {
            append("Consegna registrata")
        }
        if (late) append(" · in ritardo")
    }
    HeroCard(
        container = scheme.primaryContainer,
        accent = scheme.primary,
        onContainer = scheme.onPrimaryContainer,
        decorShape = OrganicShapes.Cookie,
        eyebrow = "❀ CONSEGNATO",
        headline = "Consegnato",
        caption = caption,
        cta = null,
        onCta = {},
        showCheck = true,
        modifier = modifier,
    )
}

@Composable
private fun GradedHero(
    status: SubmissionStatus.Graded,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = scheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .size(120.dp)
                    .graphicsLayer { alpha = 0.28f }
                    .background(scheme.primary, OrganicShapes.Sunny),
            )
            Column {
                Text(
                    text = "❀ VALUTATO",
                    color = scheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = formatGradeValue(status.grade),
                        color = scheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black,
                        fontSize = 52.sp,
                        lineHeight = 48.sp,
                        letterSpacing = (-2.5).sp,
                    )
                    if (status.maxGrade != null) {
                        Text(
                            text = "/ ${formatGradeValue(status.maxGrade)}",
                            color = scheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
                if (status.submittedAt != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Inviato ${FullDateFmt.format(status.submittedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onPrimaryContainer,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    container: Color,
    accent: Color,
    onContainer: Color,
    decorShape: androidx.compose.ui.graphics.Shape,
    eyebrow: String,
    headline: String,
    caption: String,
    cta: String?,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
    showCheck: Boolean = false,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = container,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .size(120.dp)
                    .graphicsLayer { alpha = 0.28f }
                    .background(accent, decorShape),
            )
            Column {
                Text(
                    text = eyebrow,
                    color = accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (showCheck) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(OrganicShapes.Cookie)
                                .background(accent),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = container,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleLarge,
                        color = onContainer,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainer,
                    fontStyle = FontStyle.Italic,
                )
                if (cta != null) {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = CircleShape,
                        color = onContainer,
                        modifier = Modifier.clickable(onClick = onCta),
                    ) {
                        Text(
                            text = cta,
                            color = container,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun formatCountdown(now: Instant, due: Instant): String {
    val duration = Duration.between(now, due)
    if (duration.isNegative) return "SCADUTO"
    val days = duration.toDays()
    val hours = (duration.toHours() % 24)
    val minutes = (duration.toMinutes() % 60)
    return when {
        days >= 1 -> "TRA ${days}G ${hours}H"
        hours >= 1 -> "TRA ${hours}H ${minutes}MIN"
        else -> "TRA ${minutes}MIN"
    }
}

private fun formatGradeValue(grade: Double?): String = when {
    grade == null -> "—"
    grade % 1.0 == 0.0 -> grade.toInt().toString()
    else -> String.format(Locale.ITALIAN, "%.1f", grade)
}

private val FullDateFmt = DateTimeFormatter
    .ofPattern("EEE d MMM", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val TimeFmt = DateTimeFormatter
    .ofPattern("HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
