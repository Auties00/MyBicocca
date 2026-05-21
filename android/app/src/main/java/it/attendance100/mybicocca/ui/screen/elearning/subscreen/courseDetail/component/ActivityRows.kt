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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AssignmentRow(
    assignment: DomainAssignment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val (statusLabel, gradeChip) = assignmentStatus(assignment)
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
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                }
                if (gradeChip != null) {
                    GradeChip(label = gradeChip)
                }
            }
        }
    }
}

@Composable
fun QuizRow(
    quiz: Quiz,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quiz.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = quizStatus(quiz),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

@Composable
fun ForumRow(
    forum: Forum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val isNews = forum.type == ForumType.News
    val containerColor = if (isNews) scheme.tertiaryContainer else scheme.surfaceContainerLow
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Box {
            if (isNews) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 14.dp, y = 14.dp)
                        .size(60.dp)
                        .graphicsLayer { alpha = 0.25f }
                        .background(scheme.tertiary, OrganicShapes.Puffy),
                )
            }
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ForumAvatar(
                    initials = initialsFromAuthor(forum.name).ifBlank { "·" },
                    bg = if (isNews) scheme.tertiary else scheme.primary,
                    shape = if (isNews) OrganicShapes.Cookie else OrganicShapes.Burst,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = forum.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isNews) scheme.onTertiaryContainer else scheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.1).sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${forum.discussionCount} discussioni · ${forumTypeLabel(forum.type)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isNews) scheme.onTertiaryContainer.copy(alpha = 0.78f)
                        else scheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                }
                if (forum.discussionCount > 0 && isNews) {
                    UnreadBadge(count = forum.discussionCount)
                }
            }
        }
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
private fun ForumAvatar(initials: String, bg: Color, shape: androidx.compose.ui.graphics.Shape) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(shape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(OrganicShapes.Cookie)
            .background(scheme.tertiary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.coerceAtMost(99).toString(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
        )
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

private fun assignmentStatus(a: DomainAssignment): Pair<String, String?> {
    return when (val s = a.submissionStatus) {
        SubmissionStatus.NotSubmitted -> {
            val due = a.dueDate?.let { "Scadenza ${DateFmt.format(it)}" } ?: "Da consegnare"
            due to null
        }
        is SubmissionStatus.Draft -> "Bozza salvata" to null
        is SubmissionStatus.Submitted -> {
            val when_ = s.submittedAt?.let { "Inviato ${DateFmt.format(it)}" } ?: "Inviato"
            when_ to null
        }
        is SubmissionStatus.Graded -> {
            val when_ = s.submittedAt?.let { "Valutato ${DateFmt.format(it)}" } ?: "Valutato"
            val grade = formatGradePair(s.grade, s.maxGrade)
            when_ to grade
        }
    }
}

private fun quizStatus(quiz: Quiz): String {
    val open = quiz.timeOpen
    val close = quiz.timeClose
    return when {
        close != null && Instant.now().isAfter(close) -> "Chiuso ${DateFmt.format(close)}"
        open != null && Instant.now().isBefore(open) -> "Apre ${DateFmt.format(open)}"
        close != null -> "Chiude ${DateFmt.format(close)}"
        else -> "Disponibile"
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
    ForumType.QandA -> "Q&A"
    ForumType.General -> "Generale"
    ForumType.EachUser -> "Per studente"
    ForumType.SingleSimple -> "Singolo"
    ForumType.BlogLike -> "Blog"
    ForumType.Other -> "Forum"
}

private fun initialsFromAuthor(name: String): String =
    name.split(" ", "\t").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
