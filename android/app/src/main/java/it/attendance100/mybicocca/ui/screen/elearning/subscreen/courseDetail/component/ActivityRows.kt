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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
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
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment as DomainAssignment

/**
 * Card for one assignment on the Compiti tab: a calendar-style due-date tile anchors the row,
 * followed by the name and a status line (due/submitted/draft wording, tinted error when
 * overdue and tertiary when due soon), and capped by a grade chip, submitted badge or draft
 * badge depending on the submission status. A faded organic blob decorates the corner.
 */
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

/**
 * Calendar-tile anchor: real course data repeats the same assignment name many times
 * ("Consegna esercizi laboratorio" ×7), so the date has to carry the row's identity. A missing
 * deadline — surprisingly common (18 of 42 real assignments surveyed) — renders as "∞".
 */
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
                    fontSize = 21.sp,
                    lineHeight = 22.sp,
                    letterSpacing = (-0.5).sp,
                )
                Text(
                    text = MonthFmt.format(due).uppercase().trimEnd('.'),
                    color = fg.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    letterSpacing = 0.6.sp,
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

/**
 * Row for one quiz inside the Quiz tab's expandable section cards: an organic-shape badge
 * whose icon and color encode the state (locked/scheduled/completed/available), the quiz name,
 * and a status line composing open/close dates, time limit and single-attempt notes.
 *
 * Real Bicocca data: quizzes are self-assessment batteries (221/222 surveyed allow unlimited
 * attempts, almost none have open/close windows), so the row's identity is completion state,
 * not deadlines. Completion comes from the course module completion map keyed by cmId. Rows
 * render inside the tab's expandable section cards, so they sit on surfaceContainerLowest with
 * the stacked-run shape the caller assigns — mirroring the Contenuti module rows, not
 * standalone cards.
 */
@Composable
fun QuizRow(
    quiz: Quiz,
    shape: Shape,
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
        shape = shape,
        color = scheme.surfaceContainerLowest,
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

/**
 * Row for one forum on the Forum tab: a type-keyed organic badge (Q&A gets the secondary
 * accent, others the primary), the forum name, and a one-line intro or type label. The news
 * "Avvisi" forum renders as [AnnouncementsCard], so this row covers the remaining types:
 * student forums, Q&A and per-turno teacher boards. Empty forums (common: topic forums
 * teachers pre-create and never use) drop to low emphasis.
 */
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
                        isEmpty -> stringResource(R.string.elearning_course_no_discussions) + " · " + forumTypeLabel(forum.type)
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
        }
    }
}

/**
 * Row for one gradebook item: name, optional activity-type subtitle, and a cookie-shaped grade
 * chip preferring the server-formatted grade over a raw grade/max pair.
 */
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

private val DateFmt = DateTimeFormatter
    .ofPattern("d MMM", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

private val DayFmt = DateTimeFormatter
    .ofPattern("d", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

private val MonthFmt = DateTimeFormatter
    .ofPattern("MMM", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

private val TimeFmt = DateTimeFormatter
    .ofPattern("HH:mm", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

private const val DUE_SOON_HOURS = 48L

@Composable
private fun assignmentStatusLabel(a: DomainAssignment, now: Instant): String =
    when (val s = a.submissionStatus) {
        SubmissionStatus.NotSubmitted -> {
            val due = a.dueDate
            if (due == null) stringResource(R.string.elearning_course_no_deadline) else dueLabel(now, due)
        }
        is SubmissionStatus.Draft -> {
            val due = a.dueDate
            if (due == null || due.isBefore(now)) stringResource(R.string.elearning_course_draft_saved)
            else stringResource(R.string.elearning_course_draft_saved) + " · " +
                dueLabel(now, due).replaceFirstChar { it.lowercaseChar() }
        }
        is SubmissionStatus.Submitted -> {
            val late = a.dueDate != null && s.submittedAt?.isAfter(a.dueDate) == true
            val sent = s.submittedAt?.let { stringResource(R.string.elearning_course_submitted_date, DateFmt.format(it)) }
                ?: stringResource(R.string.elearning_course_sent)
            if (late) sent + " · " + stringResource(R.string.elearning_course_submitted_late) else sent
        }
        is SubmissionStatus.Graded ->
            s.submittedAt?.let { stringResource(R.string.elearning_course_graded_date, DateFmt.format(it)) }
                ?: stringResource(R.string.elearning_course_graded)
    }

@Composable
private fun dueLabel(now: Instant, due: Instant): String {
    val zone = ZoneId.systemDefault()
    val days = ChronoUnit.DAYS.between(now.atZone(zone).toLocalDate(), due.atZone(zone).toLocalDate())
    return when {
        days < 0L -> stringResource(R.string.elearning_course_expired_days_ago, dayCount(-days))
        days == 0L -> stringResource(R.string.elearning_course_expires_today)
        else -> stringResource(R.string.elearning_course_expires_in, dayCount(days))
    }
}

@Composable
private fun dayCount(days: Long): String =
    pluralStringResource(R.plurals.elearning_course_due_days, days.toInt(), days.toInt())

@Composable
private fun quizStatusLabel(quiz: Quiz, completed: Boolean, now: Instant): String {
    val open = quiz.timeOpen
    val close = quiz.timeClose
    val parts = mutableListOf<String>()
    when {
        close != null && now.isAfter(close) -> parts += stringResource(R.string.elearning_course_closed_date, DateFmt.format(close))
        open != null && now.isBefore(open) -> parts += stringResource(R.string.elearning_course_opens_date, DateFmt.format(open))
        else -> {
            parts += if (completed) stringResource(R.string.elearning_course_completed) else stringResource(R.string.elearning_course_available)
            if (close != null) parts += closeLabel(now, close)
        }
    }
    quiz.timeLimitSeconds?.takeIf { it > 0 }?.let { parts += stringResource(R.string.elearning_course_time_limit, it / 60) }
    if (quiz.maxAttempts == 1) parts += stringResource(R.string.elearning_course_single_attempt)
    return parts.joinToString(" · ")
}

@Composable
private fun closeLabel(now: Instant, close: Instant): String {
    val zone = ZoneId.systemDefault()
    val days = ChronoUnit.DAYS.between(now.atZone(zone).toLocalDate(), close.atZone(zone).toLocalDate())
    return when {
        days == 0L -> stringResource(R.string.elearning_course_close_today, TimeFmt.format(close))
        days == 1L -> stringResource(R.string.elearning_course_close_tomorrow, TimeFmt.format(close))
        days <= 14L -> stringResource(R.string.elearning_course_close_days, days)
        else -> stringResource(R.string.elearning_course_close_date, DateFmt.format(close))
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
    if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.getDefault(), "%.1f", this)

@Composable
private fun forumTypeLabel(type: ForumType): String = stringResource(
    when (type) {
        ForumType.News -> R.string.elearning_course_announcements
        ForumType.QandA -> R.string.elearning_course_qa_forum
        ForumType.General -> R.string.elearning_course_open_discussion
        ForumType.EachUser -> R.string.elearning_course_per_user_discussion
        ForumType.SingleSimple -> R.string.elearning_course_single_discussion
        ForumType.BlogLike -> R.string.elearning_course_blog_format
        ForumType.Other -> R.string.elearning_course_generic_forum
    }
)

private fun forumTypeIcon(type: ForumType): ImageVector = when (type) {
    ForumType.News -> Icons.Outlined.Campaign
    ForumType.QandA -> Icons.AutoMirrored.Outlined.HelpOutline
    else -> Icons.Outlined.Forum
}

/** Forum intros arrive as Moodle HTML; one collapsed line is enough for the row subtitle. */
private fun stripIntro(html: String): String =
    html.replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .joinToString(" ")
