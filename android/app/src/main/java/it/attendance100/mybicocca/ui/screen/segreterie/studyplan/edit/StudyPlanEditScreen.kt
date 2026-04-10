package it.attendance100.mybicocca.ui.screen.segreterie.studyplan.edit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.SquircleAnimatedButton
import it.attendance100.mybicocca.util.rememberHapticManager

@Composable
fun StudyPlanEditProgressBar(
    viewModel: StudyPlanEditViewModel,
) {
    val state by viewModel.state.collectAsState()

    if (state.years.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        val currentIndex = state.years.indexOf(state.currentYear)
        state.years.forEachIndexed { index, year ->
            val isPastOrCurrent = index <= currentIndex
            val targetColor = if (isPastOrCurrent) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHighest
            val color by animateColorAsState(targetColor, label = "segment_$year")

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color),
            )
        }
    }
}

@Composable
fun StudyPlanEditScreen(
    onNavigateBack: () -> Unit = {},
    onRequestClose: () -> Unit = onNavigateBack,
    viewModel: StudyPlanEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val isCurrentYearValid by viewModel.isValid.collectAsState(initial = false)
    val isAllValid by viewModel.allValid.collectAsState(initial = false)
    val haptic = rememberHapticManager()
    val listState = rememberLazyListState()

    // Scroll to top when year changes
    LaunchedEffect(state.currentYear) {
        listState.animateScrollToItem(0)
    }

    LaunchedEffect(state.submitted) {
        if (state.submitted) onNavigateBack()
    }

    // Back gesture: go to previous year or prompt to exit
    BackHandler {
        if (!viewModel.isFirstYear) {
            haptic.tap()
            viewModel.previousYear()
        } else {
            onRequestClose()
        }
    }

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.error != null && state.rules.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
        }

        else -> {
            Box(Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = state.currentYear,
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) togetherWith
                                androidx.compose.animation.fadeOut(
                                    androidx.compose.animation.core.tween(
                                        300
                                    )
                                )
                    },
                    label = "year_transition"
                ) { targetYear ->
                    val rulesForYear = remember(state.rules, targetYear) {
                        state.rules.filter { it.courseYear == targetYear || it.courseYear == 0 }
                    }
                    val allSelectedCodes = remember(state.rules) {
                        state.rules.flatMap { r ->
                            r.courses.filter { it.isSelected }.map { it.activityCode }
                        }.toSet()
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 160.dp, top = 8.dp),
                    ) {
                        // Year label
                        item {
                            Text(
                                text = "${targetYear}\u00B0 Anno",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        items(rulesForYear, key = { it.choiceId }) { rule ->
                            RuleCard(
                                rule = rule,
                                onToggleCourse = { courseId ->
                                    haptic.tap()
                                    viewModel.toggleCourse(rule.choiceId, courseId)
                                },
                                isCourseEnabled = { course ->
                                    if (!course.isSelected && allSelectedCodes.contains(course.activityCode)) false
                                    else rule.isCourseSelectable(course)
                                },
                            )
                        }
                    }
                }

                // Bottom bar with navigation + submit
                BottomNavigationBar(
                    isFirstYear = viewModel.isFirstYear,
                    isLastYear = viewModel.isLastYear,
                    isCurrentYearValid = isCurrentYearValid,
                    isAllValid = isAllValid,
                    isSubmitting = state.isSubmitting,
                    error = state.error,
                    onPrevious = {
                        haptic.tap()
                        viewModel.previousYear()
                    },
                    onNext = {
                        haptic.tap()
                        viewModel.nextYear()
                    },
                    onSubmit = {
                        haptic.tap()
                        viewModel.submitPlan()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: EditableRule,
    onToggleCourse: (Long) -> Unit,
    isCourseEnabled: (EditableCourse) -> Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Rule header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rule.description.ifBlank { rule.choiceTypeDescription },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (rule.isSatisfied) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Constraint indicator
            val eMin = rule.effectiveMinCredits
            val eMax = rule.effectiveMaxCredits
            if (eMin != null || eMax != null) {
                Spacer(Modifier.height(8.dp))
                val selected = rule.selectedCredits
                val max = eMax ?: eMin ?: 0f
                val progress = if (max > 0) (selected / max).coerceIn(0f, 1f) else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = buildString {
                            if (eMin != null && eMax != null) {
                                if (eMin == eMax) {
                                    append("${eMin.toInt()} CFU")
                                } else {
                                    append("${eMin.toInt()}-${eMax.toInt()} CFU")
                                }
                            } else if (eMin != null) {
                                append("min ${eMin.toInt()} CFU")
                            } else if (eMax != null) {
                                append("max ${eMax.toInt()} CFU")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${selected.toInt()}/${max.toInt()} CFU",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (rule.isSatisfied) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (rule.isSatisfied) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }

            // Pre-note
            if (!rule.preNote.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = rule.preNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Courses
            rule.courses.forEach { course ->
                CourseRow(
                    course = course,
                    enabled = isCourseEnabled(course),
                    onToggle = { onToggleCourse(course.teachingActivityChoiceId) },
                )
            }

            // Post-note
            if (!rule.postNote.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = rule.postNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CourseRow(
    course: EditableCourse,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val isEnabled = enabled && !course.isMandatory
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .alpha(if (isEnabled || course.isSelected) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = course.isSelected,
            onCheckedChange = { onToggle() },
            enabled = isEnabled,
            colors = CheckboxDefaults.colors(
                checkedColor = if (course.isMandatory)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.primary,
            ),
        )
        Text(
            text = course.activityDescription,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Text(
                text = "${course.credits.toInt()} CFU",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    isFirstYear: Boolean,
    isLastYear: Boolean,
    isCurrentYearValid: Boolean,
    isAllValid: Boolean,
    isSubmitting: Boolean,
    error: String?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (error != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!isFirstYear) {
                        SquircleAnimatedButton(
                            onClick = onPrevious,
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.career_plan_edit_previous))
                        }
                    }

                    AnimatedContent(
                        targetState = isLastYear,
                        modifier = Modifier.weight(if (!isFirstYear) 1.5f else 1f),
                        transitionSpec = {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        },
                        label = "actionButton",
                    ) { last ->
                        if (last) {
                            SquircleAnimatedButton(
                                onClick = onSubmit,
                                enabled = isAllValid && !isSubmitting,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.career_plan_edit_submit))
                            }
                        } else {
                            SquircleAnimatedButton(
                                onClick = onNext,
                                enabled = isCurrentYearValid,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.career_plan_edit_next))
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
