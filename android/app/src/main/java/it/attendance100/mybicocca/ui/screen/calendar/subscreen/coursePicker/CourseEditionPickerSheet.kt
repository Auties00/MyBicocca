package it.attendance100.mybicocca.ui.screen.calendar.subscreen.coursePicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet

/**
 * Modal bottom sheet choosing which e-learning edition "Apri corso" should open when an
 * event's activity code resolves to more than one course — the base course plus streams,
 * or several yearly editions. A bold header counting the editions, styled after the
 * profile's "Iscrizioni" sheet, sits over a grouped list of rows showing each edition's
 * period label and full name; picking a row hands its course id to [onPick].
 */
@Composable
fun CourseEditionPickerSheet(
    courses: List<EnrolledCourse>,
    onPick: (CourseId) -> Unit,
    onDismiss: () -> Unit,
) {
    PredictiveModalBottomSheet(
        onDismiss = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sizeDuration = 500,
    ) { _, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp),
        ) {
            Header(count = courses.size)
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                itemsIndexed(courses, key = { _, course -> course.id.value }) { index, course ->
                    EditionRow(
                        course = course,
                        isFirst = index == 0,
                        isLast = index == courses.lastIndex,
                        onClick = { onPick(course.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(count: Int) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(start = 22.dp, top = 4.dp, end = 22.dp, bottom = 4.dp)) {
        Text(
            text = stringResource(R.string.course_picker_title),
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.9).sp,
            color = scheme.onSurface,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = scheme.primary, fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.course_picker_editions, count))
                }
                append(" " + stringResource(R.string.course_picker_available))
            },
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant,
        )
    }
}

/**
 * One pickable edition in the expressive grouped-list shape — large corners cap the
 * group's ends, tight ones where rows touch — with an icon tile leading the period label
 * over the course name.
 */
@Composable
private fun EditionRow(
    course: EnrolledCourse,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(
        topStart = if (isFirst) 20.dp else 6.dp,
        topEnd = if (isFirst) 20.dp else 6.dp,
        bottomStart = if (isLast) 20.dp else 6.dp,
        bottomEnd = if (isLast) 20.dp else 6.dp,
    )
    Surface(
        onClick = onClick,
        shape = shape,
        color = scheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = scheme.surfaceContainerHighest,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = course.courseCode().periodLabel
                        ?: stringResource(R.string.course_picker_transversal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Text(
                    text = course.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
