package it.attendance100.mybicocca.ui.screen.elearning.component

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.CourseLevel
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.ui.screen.elearning.ElearningTestTags
import it.attendance100.mybicocca.ui.theme.LocalDarkTheme

private val ButtonHeight = 44.dp
private val SegmentSpacing = 2.dp
private val OuterCornerRadius = 24.dp
private val InnerCornerRadius = 6.dp

/**
 * Course-home filter bar: a horizontally scrollable connected toggle group with "Tutti",
 * "Preferiti", one segment per study year of the selected career, and one per other career
 * that has courses ("Triennale", "Magistrale", …). Segments share an outer pill
 * silhouette — large corners on the group's ends, tight corners between neighbours — and
 * exactly one is always checked; the checked segment fills with the brand colour (its container
 * in dark mode) and gains a leading check glyph.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeFilterBar(
    selected: CourseFilter,
    studyYears: List<StudyYear>,
    onSelect: (CourseFilter) -> Unit,
    modifier: Modifier = Modifier,
    otherCareers: List<Career> = emptyList(),
) {
    val careerLabels = careerChipLabels(otherCareers)
    // Note: filter labels are built dynamically and contain non-translatable year values
    // Only the base "Tutti" and "Preferiti" are translatable
    val options = remember(studyYears, otherCareers, careerLabels) {
        buildList {
            add(FilterOption(CourseFilter.All, "Tutti"))
            add(FilterOption(CourseFilter.Favourites, "Preferiti"))
            studyYears.forEach { y ->
                add(FilterOption(CourseFilter.ByYear(y), "${y.value}° anno"))
            }
            otherCareers.zip(careerLabels).forEach { (career, label) ->
                add(FilterOption(CourseFilter.ByCareer(career.id), label))
            }
        }
    }

    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val colors = ToggleButtonDefaults.toggleButtonColors(
        containerColor = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
        checkedContainerColor = if (dark) scheme.primaryContainer else scheme.primary,
        checkedContentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
    )

    val scrollState = rememberScrollState()
    val haptic = rememberHapticManager()

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
                active = opt.value == selected,
                shapes = shapesForIndex(index, options.size),
                colors = colors,
                onCheck = {
                    haptic.tap()
                    onSelect(opt.value)
                },
                modifier = Modifier.testTag(ElearningTestTags.filterOption(opt.value.tagKey())),
            )
        }
    }
}

/**
 * A career chip names the career's degree level ("Triennale"), else its programme; two careers
 * with the same label are told apart by the year they started.
 */
@Composable
private fun careerChipLabels(careers: List<Career>): List<String> {
    val labels = careers.map { career ->
        when (career.level) {
            CourseLevel.Bachelor -> stringResource(R.string.elearning_course_level_bachelor)
            CourseLevel.Master -> stringResource(R.string.elearning_course_level_master)
            CourseLevel.Doctorate -> stringResource(R.string.elearning_course_level_doctorate)
            null -> career.description
        }
    }
    return labels.mapIndexed { index, label ->
        val year = careers[index].academicYear
        if (labels.count { it == label } > 1 && year > 0) "$label $year" else label
    }
}

/**
 * Stable per-option key for the filter toggle's testTag (e.g. "all", "favourites", "year:1",
 * "career:42").
 */
private fun CourseFilter.tagKey(): String = when (this) {
    CourseFilter.All -> "all"
    CourseFilter.Favourites -> "favourites"
    is CourseFilter.ByYear -> "year:${year.value}"
    is CourseFilter.ByCareer -> "career:${careerId.value}"
}

private data class FilterOption(val value: CourseFilter, val label: String)

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
