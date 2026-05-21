package it.attendance100.mybicocca.ui.screen.elearning.subscreen.courseDetail.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.ui.component.shape.OrganicShapes
import it.attendance100.mybicocca.ui.screen.elearning.theme.LocalCourseAccentPalette
import it.attendance100.mybicocca.ui.screen.elearning.theme.tripleFor

@Composable
fun CourseDetailTheme(
    courseId: CourseId,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val base = MaterialTheme.colorScheme
    val triple = LocalCourseAccentPalette.current.tripleFor(courseId)
    val scheme = remember(base, triple, dark) {
        base.withCourseAccents(triple.primary, triple.secondary, triple.tertiary, dark)
    }
    MaterialExpressiveTheme(colorScheme = scheme, content = content)
}

private fun ColorScheme.withCourseAccents(
    primary: Color,
    secondary: Color,
    tertiary: Color,
    dark: Boolean,
): ColorScheme = copy(
    primary = primary,
    onPrimary = primary.onColor(),
    primaryContainer = primary.containerOver(surfaceContainer, dark),
    onPrimaryContainer = primary.onContainer(dark),
    secondary = secondary,
    onSecondary = secondary.onColor(),
    secondaryContainer = secondary.containerOver(surfaceContainer, dark),
    onSecondaryContainer = secondary.onContainer(dark),
    tertiary = tertiary,
    onTertiary = tertiary.onColor(),
    tertiaryContainer = tertiary.containerOver(surfaceContainer, dark),
    onTertiaryContainer = tertiary.onContainer(dark),
)

private fun Color.onColor(): Color =
    if (luminance() > 0.55f) Color(0xFF1A1A1A) else Color.White

private fun Color.containerOver(surface: Color, dark: Boolean): Color {
    val alpha = if (dark) 0.32f else 0.22f
    return copy(alpha = alpha).compositeOver(surface)
}

private fun Color.onContainer(dark: Boolean): Color =
    if (dark) lerpTowards(Color.White, 0.55f)
    else lerpTowards(Color.Black, 0.55f)

private fun Color.lerpTowards(target: Color, t: Float): Color =
    Color(
        red = red + (target.red - red) * t,
        green = green + (target.green - green) * t,
        blue = blue + (target.blue - blue) * t,
        alpha = alpha,
    )

data class HeroShapeTriple(val large: Shape, val medium: Shape, val small: Shape)

private val HERO_SHAPES = listOf(
    OrganicShapes.Burst,
    OrganicShapes.Puffy,
    OrganicShapes.Cookie,
    OrganicShapes.Sunny,
    OrganicShapes.SmoothCookie6,
    OrganicShapes.SmoothCookie4,
)

fun heroShapesFor(courseId: CourseId): HeroShapeTriple {
    val n = HERO_SHAPES.size
    val base = ((courseId.value % n) + n) % n
    return HeroShapeTriple(
        large = HERO_SHAPES[base],
        medium = HERO_SHAPES[(base + 2) % n],
        small = HERO_SHAPES[(base + 4) % n],
    )
}
