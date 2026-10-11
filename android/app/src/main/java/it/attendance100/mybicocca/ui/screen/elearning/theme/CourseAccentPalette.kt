package it.attendance100.mybicocca.ui.screen.elearning.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme

/**
 * Screen-scoped palette for the e-learning course cards: a list of saturated accents plus the
 * tint strengths used to wash a card's hero and body surfaces with the active accent, the
 * star colours, and the two foregrounds for accent-filled chrome ([onAccentFor] picks between
 * them). The light and dark variants share the accent list and differ only in tint strengths
 * and star colours. [idleStar], the unfilled star, is translucent so it takes on the card's tint.
 */
@Immutable
data class CourseAccentPalette(
    val accents: List<Color>,
    val heroTint: Float,
    val bodyTint: Float,
    val favouriteStar: Color,
    val idleStar: Color,
    val onAccent: Color,
    val onAccentDark: Color,
)

private val OnAccentDark = Color(0xFF1B1B1F)

private val SharedAccents = listOf(
    Color(0xFF9333EA),
    Color(0xFF0EA5E9),
    Color(0xFFDC2626),
    Color(0xFF10B981),
    Color(0xFF06B6D4),
    Color(0xFFF59E0B),
)

val LightCourseAccentPalette = CourseAccentPalette(
    accents = SharedAccents,
    heroTint = 0.22f,
    bodyTint = 0.10f,
    favouriteStar = Color(0xFFF59E0B),
    idleStar = Color.Black.copy(alpha = 0.28f),
    onAccent = Color.White,
    onAccentDark = OnAccentDark,
)

val DarkCourseAccentPalette = CourseAccentPalette(
    accents = SharedAccents,
    heroTint = 0.32f,
    bodyTint = 0.18f,
    favouriteStar = Color(0xFFFBBF24),
    idleStar = Color.White.copy(alpha = 0.32f),
    onAccent = Color.White,
    onAccentDark = OnAccentDark,
)

val LocalCourseAccentPalette = staticCompositionLocalOf { LightCourseAccentPalette }

/**
 * Assigns a course its accent: the course id picks a palette entry by modulo, so a course keeps
 * the same colour everywhere it appears, across sessions, with no stored mapping.
 */
fun CourseAccentPalette.accentFor(courseId: CourseId): Color {
    val n = accents.size
    val idx = ((courseId.value % n) + n) % n
    return accents[idx]
}

/**
 * The foreground to draw on [background]: whichever of the light and dark ones contrasts more,
 * so labels stay readable on bright accents (amber, cyan) as well as deep ones (purple, red).
 */
fun CourseAccentPalette.onAccentFor(background: Color): Color {
    val luminance = background.luminance()
    val lightContrast = (onAccent.luminance() + 0.05f) / (luminance + 0.05f)
    val darkContrast = (luminance + 0.05f) / (onAccentDark.luminance() + 0.05f)
    return if (lightContrast >= darkContrast) onAccent else onAccentDark
}

/** Three accents for one course, used to recolour a full Material scheme on the course detail. */
data class CourseAccentTriple(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
)

/**
 * Derives a course's accent triple from evenly spaced palette entries anchored at the same
 * modulo pick as [accentFor], so the triple's primary matches the card accent.
 */
fun CourseAccentPalette.tripleFor(courseId: CourseId): CourseAccentTriple {
    val n = accents.size
    val base = ((courseId.value % n) + n) % n
    return CourseAccentTriple(
        primary = accents[base],
        secondary = accents[(base + 2) % n],
        tertiary = accents[(base + 4) % n],
    )
}

/** Installs the light or dark course-accent palette for the subtree. */
@Composable
fun ProvideCourseAccentPalette(
    dark: Boolean = LocalDarkTheme.current,
    content: @Composable () -> Unit,
) {
    val palette = if (dark) DarkCourseAccentPalette else LightCourseAccentPalette
    CompositionLocalProvider(LocalCourseAccentPalette provides palette, content = content)
}
