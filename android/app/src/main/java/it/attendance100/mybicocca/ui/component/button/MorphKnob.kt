package it.attendance100.mybicocca.ui.component.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

// The app's selection knob (Piano di Studi language): a circle that morphs into the
// sunny shape when checked, washing to the brand red with explicit white content.
// `uncheckedIcon = null` leaves the resting circle empty — radio-like, for single-choice
// pickers where "+" would wrongly read as "add".
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphKnob(
    checked: Boolean,
    modifier: Modifier = Modifier,
    uncheckedIcon: ImageVector? = Icons.Default.Add,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Sunny) }
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = motion.defaultSpatialSpec(),
        label = "knobMorph",
    )
    val container by animateColorAsState(
        targetValue = if (checked) scheme.primary else scheme.surfaceContainerHighest,
        animationSpec = motion.defaultEffectsSpec(),
        label = "knobContainer",
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(MorphPolygonShape(morph, progress))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        val icon = if (checked) Icons.Default.Check else uncheckedIcon
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) Color.White else scheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// Scales the normalized morph path up to the knob's actual size.
private class MorphPolygonShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val matrix = Matrix()
        matrix.scale(size.width, size.height)
        val path = morph.toPath(progress).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}
