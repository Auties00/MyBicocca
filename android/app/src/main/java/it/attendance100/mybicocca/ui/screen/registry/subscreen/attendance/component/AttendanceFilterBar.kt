package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

// Mirrors the elearning HomeFilterBar look: segmented toggle row, outer corners
// rounded wide, inner seams tight.
private val ButtonHeight = 44.dp
private val SegmentSpacing = 2.dp
private val OuterCornerRadius = 24.dp
private val InnerCornerRadius = 6.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AttendanceFilterBar(
    selectedYear: StudyYear?,
    studyYears: List<StudyYear>,
    onSelect: (StudyYear?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = remember(studyYears) {
        buildList {
            add(FilterOption(null, "Tutti"))
            studyYears.forEach { y ->
                add(FilterOption(y, "${y.value}° anno"))
            }
        }
    }

    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val colors = ToggleButtonDefaults.toggleButtonColors(
        containerColor = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
        checkedContainerColor = if (dark) scheme.primaryContainer else scheme.primary,
        checkedContentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
    )

    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 10.dp)
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(SegmentSpacing),
    ) {
        options.forEachIndexed { index, opt ->
            FilterToggle(
                option = opt,
                active = opt.value == selectedYear,
                shapes = shapesForIndex(index, options.size),
                colors = colors,
                onCheck = { onSelect(opt.value) },
            )
        }
    }
}

private data class FilterOption(val value: StudyYear?, val label: String)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun shapesForIndex(index: Int, count: Int): ToggleButtonShapes {
    val leadingOuter = index == 0
    val trailingOuter = index == count - 1
    val shape: Shape = RoundedCornerShape(
        topStart = if (leadingOuter) OuterCornerRadius else InnerCornerRadius,
        bottomStart = if (leadingOuter) OuterCornerRadius else InnerCornerRadius,
        topEnd = if (trailingOuter) OuterCornerRadius else InnerCornerRadius,
        bottomEnd = if (trailingOuter) OuterCornerRadius else InnerCornerRadius,
    )
    return ToggleButtonShapes(shape = shape, pressedShape = shape, checkedShape = shape)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FilterToggle(
    option: FilterOption,
    active: Boolean,
    shapes: ToggleButtonShapes,
    colors: ToggleButtonColors,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToggleButton(
        checked = active,
        onCheckedChange = { if (it) onCheck() },
        modifier = modifier.height(ButtonHeight),
        shapes = shapes,
        colors = colors,
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        if (active) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = option.label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
