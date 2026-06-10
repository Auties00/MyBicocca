package it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.EnrolmentStatus
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.addCourse.state.SearchRow

/** Width of one ancestry gutter column in the results tree. */
private val RailStep = 14.dp

/**
 * A category in the results tree. Direct hits sit on the regular row surface; waypoint
 * ancestors that only lead to hits are muted one container step down, so the eye reads them as
 * the path, not the result. Tapping either pushes the whole chain below the current level.
 */
@Composable
fun SearchTreeCategoryRow(
    row: SearchRow.Category,
    accent: Color,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    TreeRowScaffold(row = row, modifier = modifier) {
        Surface(
            onClick = { haptic.tap(); onOpen() },
            shape = RoundedCornerShape(14.dp),
            color = if (row.isMatch) scheme.surfaceContainerHigh else scheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = if (row.isMatch) 0.16f else 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = highlighted(row.name, row.nameHighlights, accent.copy(alpha = 0.30f)),
                    color = if (row.isMatch) scheme.onSurface else scheme.onSurfaceVariant,
                    fontSize = 13.5.sp,
                    fontWeight = if (row.isMatch) FontWeight.SemiBold else FontWeight.Medium,
                    lineHeight = 17.sp,
                    letterSpacing = (-0.1).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/**
 * A matching course in the results tree: same anatomy as [CatalogCourseRow], compacted to sit
 * under its ancestry rails, with marker highlights on the matched characters.
 */
@Composable
fun SearchTreeCourseRow(
    row: SearchRow.Course,
    accent: Color,
    status: EnrolmentStatus,
    onEnrol: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    TreeRowScaffold(row = row, modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = scheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = highlighted(row.course.name, row.nameHighlights, accent.copy(alpha = 0.30f)),
                        color = scheme.onSurface,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 17.sp,
                        letterSpacing = (-0.15).sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (row.course.code.isNotBlank()) {
                        Text(
                            text = highlighted(row.course.code, row.codeHighlights, accent.copy(alpha = 0.30f)),
                            color = scheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.3.sp,
                            modifier = Modifier.padding(top = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                CourseEnrolButton(status = status, accent = accent, onEnrol = onEnrol)
            }
        }
    }
}

/** Rails gutter + card slot. IntrinsicSize.Min lets the rails canvas match the card's height. */
@Composable
private fun TreeRowScaffold(
    row: SearchRow,
    modifier: Modifier = Modifier,
    card: @Composable () -> Unit,
) {
    Row(modifier = modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)) {
        TreeRails(
            parentRails = row.parentRails,
            isLastChild = row.isLastChild,
            modifier = Modifier.fillMaxHeight(),
        )
        Box(modifier = Modifier.weight(1f)) { card() }
    }
}

/**
 * Ancestry rails, thread-tree style: straight verticals for ancestors that continue below this
 * row, and an elbow in the last column hooking this row onto its parent's rail (tee when more
 * siblings follow, corner when this is the last child). Top-level rows have no gutter.
 */
@Composable
private fun TreeRails(
    parentRails: List<Boolean>,
    isLastChild: Boolean,
    modifier: Modifier = Modifier,
) {
    val depth = parentRails.size
    if (depth == 0) return
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier.width(RailStep * depth)) {
        val step = RailStep.toPx()
        val stroke = 1.5.dp.toPx()
        val midY = size.height / 2f

        for (column in 0 until depth - 1) {
            if (!parentRails[column]) continue
            val x = column * step + step / 2f
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = stroke,
            )
        }

        val elbowX = (depth - 1) * step + step / 2f
        drawLine(
            color = color,
            start = Offset(elbowX, 0f),
            end = Offset(elbowX, if (isLastChild) midY else size.height),
            strokeWidth = stroke,
        )
        drawLine(
            color = color,
            start = Offset(elbowX, midY),
            end = Offset(size.width, midY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

/**
 * Marker-style highlight: the ranges QueryHighlighter computed over the ORIGINAL string become
 * background spans, clamped defensively against any range/length mismatch.
 */
private fun highlighted(text: String, ranges: List<IntRange>, marker: Color): AnnotatedString {
    if (ranges.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        for (range in ranges) {
            val start = range.first.coerceIn(0, text.length)
            val end = (range.last + 1).coerceIn(start, text.length)
            if (end > start) addStyle(SpanStyle(background = marker), start, end)
        }
    }
}
