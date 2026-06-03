package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment as DomainAssignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionStatus
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.domain.model.elearning.grade.GradeItem
import it.attendance100.mybicocca.domain.model.elearning.quiz.Quiz
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun AssignmentRow(
    assignment: DomainAssignment,
    now: Instant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val status = assignment.submissionStatus
    val handedIn = status is SubmissionStatus.Submitted || status is SubmissionStatus.Graded
    val due = assignment.dueDate
    val overdue = !handedIn && due != null && due.isBefore(now)
    val dueSoon = !handedIn && due != null && !overdue &&
        Duration.between(now, due).toHours() < DUE_SOON_HOURS
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-12).dp, y = 12.dp)
                    .size(50.dp)
                    .graphicsLayer { alpha = 0.12f }
                    .background(scheme.primary, OrganicShapes.Leaf),
            )
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DueDateTile(due = due, overdue = overdue, dueSoon = dueSoon)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignment.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.1).sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = assignmentStatusLabel(assignment, now),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            overdue -> scheme.error
                            dueSoon -> scheme.tertiary
                            else -> scheme.onSurfaceVariant
                        },
                        fontStyle = FontStyle.Italic,
                    )
                }
                when (status) {
                    is SubmissionStatus.Graded -> {
                        val grade = formatGradePair(status.grade, status.maxGrade)
                        if (grade != null) GradeChip(label = grade) else SubmittedBadge()
                    }
                    is SubmissionStatus.Submitted -> SubmittedBadge()
                    is SubmissionStatus.Draft -> DraftBadge()
                    SubmissionStatus.NotSubmitted -> Unit
                }
            }
        }
    }
}

// Calendar-tile anchor: real course data repeats the same assignment name many times
// ("Consegna esercizi laboratorio" x7), so the date has to carry the row's identity.
@Composable
private fun DueDateTile(due: Instant?, overdue: Boolean, dueSoon: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val bg = when {
        overdue -> scheme.errorContainer
        dueSoon -> scheme.tertiaryContainer
        else -> scheme.surfaceContainerHigh
    }
    val fg = when {
        overdue -> scheme.onErrorContainer
        dueSoon -> scheme.onTertiaryContainer
        else -> scheme.onSurface
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        if (due == null) {
            // No deadline at all — surprisingly common (18 of 42 real assignments surveyed).
            Text(
                text = "∞",
                color = fg.copy(alpha = 0.75f),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = DayFmt.format(due),
                    color = fg,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = MonthFmt.format(due).uppercase().trimEnd('.'),
                    color = fg.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    letterSpacing = 1.2.sp,
                )
            }
        }
    }
}

@Composable
private fun SubmittedBadge() {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(OrganicShapes.Cookie)
            .background(scheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = "Consegnato",
            tint = scheme.onPrimary,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun DraftBadge() {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(scheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = "BOZZA",
            color = scheme.onSecondaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
        )
    }
}

// Real Bicocca data: quizzes are self-assessment batteries (221/222 surveyed allow unlimited
// attempts, almost none have open/close windows), so the row's identity is completion state,
// not deadlines. Completion comes from the course module completion map keyed by cmId.
@Composable
fun QuizRow(
    quiz: Quiz,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    completed: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val now = Instant.now()
    val closed = quiz.timeClose != null && now.isAfter(quiz.timeClose)
    val notYetOpen = quiz.timeOpen != null && now.isBefore(quiz.timeOpen)
    val locked = closed || notYetOpen
    val closingSoon = !locked && !completed && quiz.timeClose != null &&
        Duration.between(now, quiz.timeClose).toHours() < DUE_SOON_HOURS
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(if (completed) OrganicShapes.Cookie else OrganicShapes.Puffy)
                    .background(
                        when {
                            locked -> scheme.surfaceContainerHighest
                            completed -> scheme.primary
                            else -> scheme.secondary
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when {
                        notYetOpen -> Icons.Outlined.Schedule
                        closed -> Icons.Outlined.Lock
                        completed -> Icons.Rounded.Check
                        else -> Icons.Outlined.Quiz
                    },
                    contentDescription = null,
                    tint = when {
                        locked -> scheme.onSurfaceVariant
                        completed -> scheme.onPrimary
                        else -> scheme.onSecondary
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quiz.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (locked) scheme.onSurfaceVariant else scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.1).sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = quizStatusLabel(quiz, completed, now),
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        closed && !completed -> scheme.error
                        closingSoon -> scheme.tertiary
                        else -> scheme.onSurfaceVariant
                    },
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// The news "Avvisi" forum renders as AnnouncementsCard, so this row covers the remaining
// types: student forums, Q&A and per-turno teacher boards. Empty forums (common: topic
// forums teachers pre-create and never use) drop to low emphasis.
@Composable
fun ForumRow(
    forum: Forum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val isEmpty = forum.discussionCount == 0
    val isQandA = forum.type == ForumType.QandA
    val accent = if (isQandA) scheme.secondary else scheme.primary
    val onAccent = if (isQandA) scheme.onSecondary else scheme.onPrimary
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(if (isQandA) OrganicShapes.Sunny else OrganicShapes.Burst)
                    .background(if (isEmpty) scheme.surfaceContainerHighest else accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = forumTypeIcon(forum.type),
                    contentDescription = null,
                    tint = if (isEmpty) scheme.onSurfaceVariant else onAccent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = forum.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isEmpty) scheme.onSurfaceVariant else scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.1).sp,
                )
                Spacer(Modifier.height(2.dp))
                val intro = forum.intro?.let(::stripIntro)
                Text(
                    text = when {
                        isEmpty -> "Ancora nessuna discussione · ${forumTypeLabel(forum.type)}"
                        !intro.isNullOrBlank() -> intro
                        else -> forumTypeLabel(forum.type)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!isEmpty) {
                DiscussionCountChip(count = forum.discussionCount)
            }
        }
    }
}

@Composable
private fun DiscussionCountChip(count: Int) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(OrganicShapes.Cookie)
            .background(scheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = count.coerceAtMost(999).toString(),
            color = scheme.onPrimaryContainer,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
        )
    }
}

@Composable
fun GradeRow(
    item: GradeItem,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = scheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val activitySubtitle = item.activityType?.takeIf { it.isNotBlank() }
                if (activitySubtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = activitySubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
            val label = formatGrade(item)
            if (label != null) GradeChip(label = label)
        }
    }
}

@Composable
private fun GradeChip(label: String) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(OrganicShapes.Cookie)
            .background(scheme.primaryContainer)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            color = scheme.onPrimaryContainer,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun ActivityEmpty(message: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
    }
}

private val DateFmt = DateTimeFormatter
    .ofPattern("d MMM", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val DayFmt = DateTimeFormatter
    .ofPattern("d", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val MonthFmt = DateTimeFormatter
    .ofPattern("MMM", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private val TimeFmt = DateTimeFormatter
    .ofPattern("HH:mm", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

private const val DUE_SOON_HOURS = 48L

private fun assignmentStatusLabel(a: DomainAssignment, now: Instant): String =
    when (val s = a.submissionStatus) {
        SubmissionStatus.NotSubmitted -> {
            val due = a.dueDate
            if (due == null) "Senza scadenza" else dueLabel(now, due)
        }
        is SubmissionStatus.Draft -> {
            val due = a.dueDate
            if (due == null || due.isBefore(now)) "Bozza salvata"
            else "Bozza salvata · ${dueLabel(now, due).replaceFirstChar { it.lowercaseChar() }}"
        }
        is SubmissionStatus.Submitted -> {
            val late = a.dueDate != null && s.submittedAt?.isAfter(a.dueDate) == true
            val sent = s.submittedAt?.let { "Inviato il ${DateFmt.format(it)}" } ?: "Inviato"
            if (late) "$sent · in ritardo" else sent
        }
        is SubmissionStatus.Graded ->
            s.submittedAt?.let { "Valutato · inviato il ${DateFmt.format(it)}" } ?: "Valutato"
    }

private fun dueLabel(now: Instant, due: Instant): String {
    val zone = ZoneId.systemDefault()
    val days = ChronoUnit.DAYS.between(now.atZone(zone).toLocalDate(), due.atZone(zone).toLocalDate())
    return when {
        days < 0L -> "Scaduto il ${DateFmt.format(due)}"
        days == 0L -> "Scade oggi alle ${TimeFmt.format(due)}"
        days == 1L -> "Scade domani alle ${TimeFmt.format(due)}"
        days <= 14L -> "Scade tra $days giorni"
        else -> "Scadenza ${DateFmt.format(due)}"
    }
}

private fun quizStatusLabel(quiz: Quiz, completed: Boolean, now: Instant): String {
    val open = quiz.timeOpen
    val close = quiz.timeClose
    val parts = mutableListOf<String>()
    when {
        close != null && now.isAfter(close) -> parts += "Chiuso il ${DateFmt.format(close)}"
        open != null && now.isBefore(open) -> parts += "Apre il ${DateFmt.format(open)}"
        else -> {
            parts += if (completed) "Completato" else "Disponibile"
            if (close != null) parts += closeLabel(now, close)
        }
    }
    quiz.timeLimitSeconds?.takeIf { it > 0 }?.let { parts += "${it / 60} min" }
    if (quiz.maxAttempts == 1) parts += "tentativo singolo"
    return parts.joinToString(" · ")
}

private fun closeLabel(now: Instant, close: Instant): String {
    val zone = ZoneId.systemDefault()
    val days = ChronoUnit.DAYS.between(now.atZone(zone).toLocalDate(), close.atZone(zone).toLocalDate())
    return when {
        days == 0L -> "chiude oggi alle ${TimeFmt.format(close)}"
        days == 1L -> "chiude domani alle ${TimeFmt.format(close)}"
        days <= 14L -> "chiude tra $days giorni"
        else -> "chiude il ${DateFmt.format(close)}"
    }
}

private fun formatGrade(item: GradeItem): String? {
    if (item.gradeFormatted != null && item.gradeFormatted.isNotBlank()) return item.gradeFormatted
    return formatGradePair(item.grade, item.maxGrade)
}

private fun formatGradePair(grade: Double?, max: Double?): String? {
    val g = grade ?: return null
    return if (max != null) "${g.trimZero()}/${max.trimZero()}" else g.trimZero()
}

private fun Double.trimZero(): String =
    if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.ITALIAN, "%.1f", this)

private fun forumTypeLabel(type: ForumType): String = when (type) {
    ForumType.News -> "Avvisi"
    ForumType.QandA -> "Domande e risposte"
    ForumType.General -> "Discussione aperta"
    ForumType.EachUser -> "Una discussione per studente"
    ForumType.SingleSimple -> "Discussione singola"
    ForumType.BlogLike -> "Formato blog"
    ForumType.Other -> "Forum"
}

private fun forumTypeIcon(type: ForumType): ImageVector = when (type) {
    ForumType.News -> Icons.Outlined.Campaign
    ForumType.QandA -> Icons.AutoMirrored.Outlined.HelpOutline
    else -> Icons.Outlined.Forum
}

// Forum intros arrive as Moodle HTML; one collapsed line is enough for the row subtitle.
private fun stripIntro(html: String): String =
    html.replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .joinToString(" ")
