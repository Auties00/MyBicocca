package it.attendance100.mybicocca.ui.screen.elearning.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.elearning.course.AcademicYear
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import it.attendance100.mybicocca.ui.component.shape.ShapeDecorations
import it.attendance100.mybicocca.ui.screen.elearning.theme.LocalCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.ProvideCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.heroShapesFor
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import it.attendance100.mybicocca.ui.theme.PreviewBgDark
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.random.Random


// A single edition rendered on the card's top tab strip. `id` lets the host
// remember which tab is active across recomposition; `accent` is per-edition so
// the active edition drives the card's coloring.
data class CardEdition(
    val id: CourseId,
    val yearLabel: String,
    val accent: Color,
    val academicYear: AcademicYear?,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotebookCard(
    title: String,
    code: String?,
    editions: List<CardEdition>,
    activeEditionId: CourseId,
    isFavourite: Boolean,
    deadlines: List<Deadline>,
    onClick: () -> Unit,
    onSelectEdition: (CourseId) -> Unit,
    onToggleFavourite: (Boolean) -> Unit,
    onDeadlineClick: (Deadline) -> Unit,
    modifier: Modifier = Modifier,
    forcedHeroPlacements: List<ShapeDecorations.Placement>? = null,
    forcedBodyPlacements: List<ShapeDecorations.Placement>? = null,
) {
    val activeEdition = editions.firstOrNull { it.id == activeEditionId } ?: editions.first()
    val accent = activeEdition.accent
    val scheme = MaterialTheme.colorScheme
    val palette = LocalCourseAccentPalette.current
    val topBg = lerp(scheme.surfaceContainer, accent, palette.heroTint)
    val bodyBg = lerp(scheme.surfaceContainer, accent, palette.bodyTint)

    val visible = deadlines.take(3)
    val extra = (deadlines.size - visible.size).coerceAtLeast(0)
    val hasDeadlines = visible.isNotEmpty()

    // Shape decoration: the same per-course triple the course detail hero draws, keyed off the
    // active edition's id (what tapping the card opens) so a notebook and its detail screen share
    // one visual identity. Positions stay stable per notebook; only the shapes + colour follow the
    // active edition.
    val decorationId = code ?: title
    val heroTriple = remember(activeEdition.id) { heroShapesFor(activeEdition.id) }
    val decorShapes = remember(heroTriple) {
        listOf(heroTriple.large, heroTriple.medium, heroTriple.small)
    }
    val heroWatermark = lerp(topBg, accent, WatermarkTint)
    val bodyWatermark = lerp(bodyBg, accent, WatermarkTint)

    val heroPlacements = forcedHeroPlacements ?: remember(decorationId) {
        ShapeDecorations.placementsFor(decorationId, count = 3, salt = 0)
    }
    val bodyPlacements = forcedBodyPlacements ?: remember(decorationId, deadlines.size) {
        // Roughly one watermark per two deadlines, so the box doesn't get noisy as the list grows.
        val bodyCount = (deadlines.size + 1) / 2
        ShapeDecorations.placementsFor(decorationId, count = bodyCount, salt = 1)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        YearTabStrip(
            editions = editions,
            activeEditionId = activeEdition.id,
            onSelectEdition = onSelectEdition,
            foreground = palette.onAccent,
            modifier = Modifier.padding(start = 18.dp),
        )

        val heroShape = if (hasDeadlines) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        } else {
            RoundedCornerShape(16.dp)
        }

        // Hero box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(heroShape)
                .background(topBg)
                .clickable(onClick = onClick),
        ) {
            ShapeDecorationLayer(
                placements = heroPlacements,
                shapes = decorShapes,
                color = heroWatermark,
                modifier = Modifier.matchParentSize(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 14.dp, end = 6.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp,
                        lineHeight = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!code.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = code,
                            color = scheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                FavouriteStar(
                    filled = isFavourite,
                    onClick = { onToggleFavourite(!isFavourite) },
                )
            }
        }

        if (hasDeadlines) {
            Spacer(Modifier.height(2.dp))
            val today = remember { LocalDate.now() }
            val bodyShape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)

            // Deadlines box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bodyShape)
                    .background(bodyBg),
            ) {
                ShapeDecorationLayer(
                    placements = bodyPlacements,
                    shapes = decorShapes,
                    color = bodyWatermark,
                    modifier = Modifier.matchParentSize(),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                ) {
                    visible.forEach { d ->
                        DeadlineRow(
                            deadline = d,
                            today = today,
                            onClick = { onDeadlineClick(d) },
                        )
                    }
                    if (extra > 0) {
                        Text(
                            text = "+$extra ${if (extra == 1) "altra scadenza" else "altre scadenze"}",
                            color = scheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

// Strength of the accent watermark over each box background. Tune to taste.
private const val WatermarkTint = 0.18f

/**
 * Renders the deterministic [ShapeDecorations.Placement]s as filled, clipped shapes.
 * Sits between a box's background and its content; overflow is clipped by the parent's own clip.
 *
 * [shapes] is the per-course triple (large/medium/small); each placement takes one shape by index
 * and the list cycles, so the three course shapes are all represented across the decorations.
 */
@Composable
private fun ShapeDecorationLayer(
    placements: List<ShapeDecorations.Placement>,
    shapes: List<Shape>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (placements.isEmpty() || shapes.isEmpty()) return
    BoxWithConstraints(modifier = modifier) {
        val boxWidth = maxWidth
        val boxHeight = maxHeight
        placements.forEachIndexed { index, p ->
            val shapeSize = boxHeight * p.sizeFraction
            Box(
                modifier = Modifier
                    .requiredSize(shapeSize)
                    .offset(
                        x = boxWidth * p.centerXFraction - shapeSize / 2,
                        y = boxHeight * p.centerYFraction - shapeSize / 2,
                    )
                    .rotate(p.rotationDegrees)
                    .clip(shapes[index % shapes.size])
                    .background(color),
            )
        }
    }
}

@Composable
private fun YearTabStrip(
    editions: List<CardEdition>,
    activeEditionId: CourseId,
    onSelectEdition: (CourseId) -> Unit,
    foreground: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        editions.forEach { edition ->
            val active = edition.id == activeEditionId
            YearTab(
                label = edition.yearLabel,
                background = edition.accent,
                foreground = foreground,
                active = active,
                onClick = { onSelectEdition(edition.id) },
            )
        }
    }
}

@Composable
private fun YearTab(
    label: String,
    background: Color,
    foreground: Color,
    active: Boolean,
    onClick: () -> Unit,
) {
    // Inactive tabs are shorter (recessed) and dimmed so the active edition
    // visually "comes forward". When the card has a single edition both
    // values resolve to identical chrome to the pre-merge design.
    val verticalPad = if (active) 5.dp else 3.dp
    val bottomPad = if (active) 3.dp else 1.dp
    val alpha = if (active) 1f else 0.55f
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(background.copy(alpha = alpha))
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 12.dp, top = verticalPad, bottom = bottomPad),
    ) {
        Text(
            text = label.uppercase(),
            color = foreground,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun FavouriteStar(filled: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val palette = LocalCourseAccentPalette.current
    val tint = if (filled) palette.favouriteStar else scheme.onSurfaceVariant
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = if (filled) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun DeadlineRow(
    deadline: Deadline,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val simplified = deadline.title.substringAfter(" · ", missingDelimiterValue = deadline.title)
    val dueDate = deadline.dueAt.atZone(ZoneId.systemDefault()).toLocalDate()
    val daysLeft = ChronoUnit.DAYS.between(today, dueDate).toInt()

    val isToday = daysLeft == 0
    val overdue = daysLeft < 0
    val urgent = !overdue && !isToday && daysLeft <= 3

    val dueText = when {
        isToday -> "entro oggi"
        overdue -> if (abs(daysLeft) == 1) "scaduto 1 giorno fa" else "scaduto ${abs(daysLeft)} giorni fa"
        daysLeft == 1 -> "entro 1 giorno"
        else -> "entro $daysLeft giorni"
    }
    val dueColor = when {
        isToday || overdue -> scheme.error
        urgent -> scheme.onSurface
        else -> scheme.onSurface.copy(alpha = 0.70f)
    }
    val dueWeight = if (isToday || overdue || urgent) FontWeight.SemiBold else FontWeight.Medium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = simplified,
            color = scheme.onSurface,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = dueText,
            color = dueColor,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = dueWeight,
            letterSpacing = 0.1.sp,
            maxLines = 1,
        )
    }
}


@Preview(
    showBackground = true,
    widthDp = 380,
    backgroundColor = PreviewBgDark
)
@Composable
fun PreviewNotebookCard_Pos0() = NotebookCardPosPreview(0)


@Preview(
    showBackground = true,
    widthDp = 380,
    backgroundColor = PreviewBgDark
)
@Composable
fun PreviewNotebookCard_Pos1() = NotebookCardPosPreview(1)


@Preview(
    showBackground = true,
    widthDp = 380,
    backgroundColor = PreviewBgDark
)
@Composable
fun PreviewNotebookCard_Pos2() = NotebookCardPosPreview(2)


@Preview(
    showBackground = true,
    widthDp = 380,
    backgroundColor = PreviewBgDark
)
@Composable
fun PreviewNotebookCard_Pos3() = NotebookCardPosPreview(3)


@Preview(
    showBackground = true,
    widthDp = 380,
    backgroundColor = PreviewBgDark
)
@Composable
fun PreviewNotebookCard_Pos4() = NotebookCardPosPreview(4)


@Preview(
    showBackground = true,
    widthDp = 380,
    backgroundColor = PreviewBgDark
)
@Composable
fun PreviewNotebookCard_Pos5() = NotebookCardPosPreview(5)

@Composable
private fun NotebookCardPosPreview(posIndex: Int) {
    val placements = ShapeDecorations.previewLayout(layoutIndex = posIndex, count = 3)

    fun randomColor(alpha: Int = 255) = Color(
        Random.nextInt(256),
        Random.nextInt(256),
        Random.nextInt(256),
        alpha = alpha
    )


    BicoccaTheme(dark = true) {
        ProvideCourseAccentPalette(dark = true) {
            NotebookCard(
                title = "Programmazione Mobile",
                code = "INF01-$posIndex",
                editions = listOf(
                    CardEdition(CourseId(1), "2023/24", randomColor(), null)
                ),
                activeEditionId = CourseId(1),
                isFavourite = false,
                deadlines = emptyList(),
                onClick = {},
                onSelectEdition = {},
                onToggleFavourite = {},
                onDeadlineClick = {},
                forcedHeroPlacements = placements
            )
        }
    }
}
