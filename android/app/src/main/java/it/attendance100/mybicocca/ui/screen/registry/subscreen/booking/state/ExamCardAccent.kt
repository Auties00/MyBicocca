package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import kotlin.math.absoluteValue

data class ExamCardAccent(
    val shape: Shape,
    val accent: Color,
    val onAccent: Color,
)

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun accentForCourse(courseKey: String, scheme: ColorScheme): ExamCardAccent {
    val bucket = courseKey.hashCode().absoluteValue
    val shape = when (bucket % 6) {
        0 -> MaterialShapes.Sunny
        1 -> MaterialShapes.Cookie9Sided
        2 -> MaterialShapes.Clover4Leaf
        3 -> MaterialShapes.Pentagon
        4 -> MaterialShapes.Flower
        else -> MaterialShapes.Cookie6Sided
    }.toShape()
    val (accent, onAccent) = when (bucket % 4) {
        0 -> scheme.primary to scheme.onPrimary
        1 -> scheme.tertiary to scheme.onTertiary
        2 -> scheme.secondary to scheme.onSecondary
        else -> scheme.onPrimaryContainer to scheme.primaryContainer
    }
    return ExamCardAccent(shape = shape, accent = accent, onAccent = onAccent)
}
