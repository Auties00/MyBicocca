package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.studyplan.ChoiceConstraintUnit
import it.attendance100.mybicocca.domain.model.studyplan.EditableCourse
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.ui.component.button.MorphKnob
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.navigation.AppRoute
import it.attendance100.mybicocca.ui.screen.registry.state.RegistryBadgeTone
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state.StudyPlanEditEvent
import it.attendance100.mybicocca.ui.screen.registry.theme.registryBadgeTone
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Plan compiler pager: one button-driven page per year, rule groups in the Piano di
// Studi connected-segment style with morphing selection knobs, and the app's connected
// button pair at the bottom stepping the pager, morphing Avanti into Invia at the end.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StudyPlanEditScreen(
    studentId: Long,
    choiceRegulationId: Long,
    schemaId: Long,
    planId: Long,
    onClose: () -> Unit = {},
    viewModel: StudyPlanEditViewModel = hiltViewModel<StudyPlanEditViewModel, StudyPlanEditViewModel.Factory>(
        creationCallback = { factory: StudyPlanEditViewModel.Factory ->
            factory.create(
                AppRoute.StudyPlanEdit(
                    studentId = studentId,
                    choiceRegulationId = choiceRegulationId,
                    schemaId = schemaId,
                    planId = planId,
                )
            )
        },
    ),
) {
    val rulesData by viewModel.rules.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val currentYear by viewModel.currentYear.collectAsStateWithLifecycle()
    val submitting by viewModel.submitting.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                StudyPlanEditEvent.Submitted -> {
                    scope.launch { snackbar.showInfo("Piano di studi inviato") }
                    onClose()
                }
            }
        }
    }

    // Submit failures surface as snackbars; validity already lives in the rule pills.
    LaunchedEffect(viewModel) {
        viewModel.submitError.collectLatest { message ->
            if (message != null) snackbar.showError(message)
        }
    }

    val rules = rulesData.valueOrNull()

    when {
        rules == null -> when (val status = syncStatus) {
            is SyncStatus.Failed -> ErrorEmptyState(cause = status.cause, onRetry = viewModel::refresh)
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator(modifier = Modifier.size(72.dp))
            }
        }

        rules.isEmpty() -> EmptyState(
            icon = Icons.Outlined.Lock,
            title = "Nessuna regola da compilare",
            body = "Lo schema del piano non contiene regole di scelta.",
        )

        else -> PlanCompilerPager(
            rules = rules,
            initialYear = currentYear,
            submitting = submitting,
            onYearSettled = viewModel::setYear,
            onToggleCourse = viewModel::toggleCourse,
            onSubmit = viewModel::submit,
            onClose = onClose,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlanCompilerPager(
    rules: List<EditableRule>,
    initialYear: Int,
    submitting: Boolean,
    onYearSettled: (Int) -> Unit,
    onToggleCourse: (ruleChoiceId: Long, courseChoiceId: Long) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    // Year-agnostic rules (courseYear == 0) get their own closing page instead of
    // repeating on every year.
    val segments = remember(rules) {
        val years = rules.years()
        if (rules.any { it.courseYear == 0 }) years + 0 else years
    }
    val isAllValid = remember(rules) { rules.all { it.isSatisfied } }
    // An activity picked under any rule can't be picked again elsewhere.
    val selectedCodes = remember(rules) {
        rules.flatMap { rule -> rule.courses.filter { it.isSelected }.map { it.code } }.toSet()
    }
    val hasChanges = remember(rules) {
        rules.any { rule -> rule.courses.any { it.isSelected != it.isInitialSelected } }
    }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = segments.indexOf(initialYear).coerceAtLeast(0),
    ) { segments.size }

    // Keep the ViewModel's year as the survives-config-change record of where we are.
    LaunchedEffect(pagerState, segments) {
        snapshotFlow { pagerState.settledPage }.collectLatest { page ->
            segments.getOrNull(page)?.let(onYearSettled)
        }
    }

    // System back walks the pager first; leaving with unsent picks deserves a heads-up.
    var showDiscardDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = pagerState.currentPage > 0 || (hasChanges && !submitting)) {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else {
            showDiscardDialog = true
        }
    }
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Uscire senza inviare?") },
            text = { Text("Le scelte fatte finora non sono state inviate e andranno perse.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onClose()
                    },
                ) { Text("Esci") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Continua") }
            },
        )
    }

    Column(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            key = { segments[it] },
            // The bottom bar drives the wizard; pages don't react to swipes.
            userScrollEnabled = false,
        ) { page ->
            val segment = segments[page]
            val segmentRules = remember(rules, segment) {
                rules.filter { it.courseYear == segment }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(segmentRules, key = { it.choiceId }) { rule ->
                    RuleGroup(
                        rule = rule,
                        onToggleCourse = { courseId -> onToggleCourse(rule.choiceId, courseId) },
                        isCourseEnabled = { course ->
                            if (!course.isSelected && course.code in selectedCodes) false
                            else rule.isCourseSelectable(course)
                        },
                    )
                }
            }
        }

        // Avanti is gated on the visible page's rules; Invia on the whole plan.
        val currentSegment = segments.getOrNull(pagerState.currentPage)
        val isPageValid = remember(rules, currentSegment) {
            rules.filter { it.courseYear == currentSegment }.all { it.isSatisfied }
        }

        WizardBottomBar(
            pageCount = segments.size,
            currentPage = pagerState.currentPage,
            canAdvance = isPageValid,
            canSubmit = isAllValid && !submitting,
            submitting = submitting,
            onBack = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            },
            onNext = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            onSubmit = onSubmit,
        )
    }
}

// A rule in the Piano di Studi segment language: header tile, one connected course tile
// per activity, and an optional closing note segment. Mandatory rules render the same
// way, just without interaction.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RuleGroup(
    rule: EditableRule,
    onToggleCourse: (Long) -> Unit,
    isCourseEnabled: (EditableCourse) -> Boolean,
) {
    val locked = rule.isMandatoryRule
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        RuleHeaderTile(rule = rule)
        val hasNote = !rule.postNote.isNullOrBlank()
        rule.courses.forEachIndexed { index, course ->
            CourseTile(
                course = course,
                enabled = !locked && isCourseEnabled(course),
                locked = locked,
                isLast = index == rule.courses.lastIndex && !hasNote,
                onToggle = { onToggleCourse(course.choiceId) },
            )
        }
        if (hasNote) {
            NoteTile(text = rule.postNote.orEmpty())
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RuleHeaderTile(rule: EditableRule) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = rule.description.ifBlank { rule.typeDescription },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = rule.subtitle(),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(8.dp))
                val target = rule.effectiveMaxUnits ?: rule.effectiveMinUnits
                RuleProgressPill(
                    selected = rule.selectedUnits.toInt(),
                    target = target?.toInt(),
                    unitLabel = if (rule.unit == ChoiceConstraintUnit.Activities) "attività" else "CFU",
                    satisfied = rule.isSatisfied,
                )
            }
            if (!rule.preNote.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = rule.preNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Progress pill in the rule's own unit (CFU or picks): the registry status tones —
// green with a check while the rule holds, warning orange with an x while it doesn't.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RuleProgressPill(selected: Int, target: Int?, unitLabel: String, satisfied: Boolean) {
    val motion = MaterialTheme.motionScheme
    val okTone = registryBadgeTone(RegistryBadgeTone.Ok)
    val warnTone = registryBadgeTone(RegistryBadgeTone.Attention)
    val container by animateColorAsState(
        targetValue = if (satisfied) okTone.container else warnTone.container,
        animationSpec = motion.defaultEffectsSpec(),
        label = "pillContainer",
    )
    val content by animateColorAsState(
        targetValue = if (satisfied) okTone.onContainer else warnTone.onContainer,
        animationSpec = motion.defaultEffectsSpec(),
        label = "pillContent",
    )
    Surface(shape = CircleShape, color = container, contentColor = content) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The leading mark flips between check and x with the rule's state.
            AnimatedContent(
                targetState = satisfied,
                transitionSpec = {
                    (scaleIn(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec())) togetherWith
                        (scaleOut(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()))
                },
                label = "pillMark",
            ) { ok ->
                Icon(
                    imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (target != null) "$selected/$target $unitLabel" else "$selected $unitLabel",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

// A connected course tile in the Piano di Studi style: leading CFU chip, name with the
// course code as caption, and a trailing knob that morphs circle-to-sunny when picked
// while the whole segment washes to the primary container.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CourseTile(
    course: EditableCourse,
    enabled: Boolean,
    locked: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val selected = course.isSelected
    val checked = selected || locked || course.isMandatory
    val interactive = !locked && enabled && !course.isMandatory

    // Selection lives in the chip and the knob: the brand red fills with white content.
    // The tile itself doesn't change.
    val chipChecked = selected && !locked
    val chipColor by animateColorAsState(
        targetValue = if (chipChecked) scheme.primary else scheme.primaryContainer,
        animationSpec = motion.defaultEffectsSpec(),
        label = "chipColor",
    )
    val chipContent by animateColorAsState(
        targetValue = if (chipChecked) Color.White else scheme.onPrimaryContainer,
        animationSpec = motion.defaultEffectsSpec(),
        label = "chipContent",
    )

    val large = 20.dp
    val small = 4.dp
    Surface(
        onClick = onToggle,
        enabled = interactive,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (interactive || checked) 1f else 0.45f),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = small,
            topEnd = small,
            bottomStart = if (isLast) large else small,
            bottomEnd = if (isLast) large else small,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(chipColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${course.credits.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = chipContent,
                        maxLines = 1,
                    )
                    Text(
                        text = "CFU",
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = chipContent.copy(alpha = 0.8f),
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (course.code.isNotBlank()) {
                    Text(
                        text = course.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            MorphKnob(checked = checked)
        }
    }
}

@Composable
private fun NoteTile(text: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurfaceVariant,
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

// The app's connected button pair (see EventDetailSheet/ExamResultsScreen): an
// icon-only tonal back that springs in past the first page, and the primary action
// morphing between Avanti and Invia. Broken rules disable the primary action.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WizardBottomBar(
    pageCount: Int,
    currentPage: Int,
    canAdvance: Boolean,
    canSubmit: Boolean,
    submitting: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val spatialOffsetSpec = motion.defaultSpatialSpec<IntOffset>()
    val effectsFloatSpec = motion.defaultEffectsSpec<Float>()
    val isLast = currentPage == pageCount - 1
    val backVisible = currentPage > 0 && !submitting

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (pageCount > 1) {
            PageDots(pageCount = pageCount, currentPage = currentPage)
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AnimatedVisibility(
                visible = backVisible,
                enter = expandHorizontally(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkHorizontally(motion.defaultSpatialSpec()) + fadeOut(motion.defaultEffectsSpec()),
            ) {
                FilledTonalButton(
                    onClick = onBack,
                    modifier = Modifier
                        .width(64.dp)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHigh,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Pagina precedente",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // The primary action stays a full pill alone and flattens its start corners
            // when the back button joins the pair.
            val startCorner by animateDpAsState(
                targetValue = if (backVisible) 8.dp else 28.dp,
                animationSpec = motion.defaultSpatialSpec(),
                label = "actionCorner",
            )
            // One persistent button: only its label and icon slide between the
            // Avanti / Invia / in-flight states.
            val action = when {
                isLast && submitting -> WizardAction.Sending
                isLast -> WizardAction.Submit
                else -> WizardAction.Next
            }
            Button(
                onClick = {
                    when (action) {
                        WizardAction.Next -> onNext()
                        WizardAction.Submit -> onSubmit()
                        WizardAction.Sending -> Unit
                    }
                },
                enabled = if (isLast) canSubmit || submitting else canAdvance,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(
                    topStart = startCorner,
                    bottomStart = startCorner,
                    topEnd = 28.dp,
                    bottomEnd = 28.dp,
                ),
                // Brand red always pairs with white content, in dark mode too.
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = Color.White,
                ),
            ) {
                AnimatedContent(
                    targetState = action,
                    transitionSpec = {
                        (slideInHorizontally(spatialOffsetSpec) { it / 3 } + fadeIn(effectsFloatSpec)) togetherWith
                            (slideOutHorizontally(spatialOffsetSpec) { -it / 3 } + fadeOut(effectsFloatSpec))
                    },
                    label = "wizardActionLabel",
                ) { target ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (target) {
                            WizardAction.Next -> {
                                Text("Avanti", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            WizardAction.Submit -> {
                                Text("Invia piano", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            WizardAction.Sending -> {
                                LoadingIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Invio in corso…", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class WizardAction { Next, Submit, Sending }

// Page indicator: the active dot stretches into a primary pill on spring physics.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PageDots(pageCount: Int, currentPage: Int) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            val width by animateDpAsState(
                targetValue = if (active) 26.dp else 8.dp,
                animationSpec = motion.defaultSpatialSpec(),
                label = "dotWidth",
            )
            val color by animateColorAsState(
                targetValue = if (active) scheme.primary else scheme.surfaceContainerHighest,
                animationSpec = motion.defaultEffectsSpec(),
                label = "dotColor",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .background(color, CircleShape),
            )
        }
    }
}

@Composable
private fun ErrorEmptyState(cause: Throwable, onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Default.Warning,
        title = "Caricamento non riuscito",
        body = cause.friendlyMessage(),
        action = { FilledTonalButton(onClick = onRetry) { Text("Riprova") } },
    )
}

// The subtitle states what the rule requires, not what it offers: "2 attività da
// scegliere", "Da 8 a 12 CFU da scegliere", "Fino a 2 attività da scegliere" (a rule
// can also require nothing).
private fun EditableRule.subtitle(): String {
    if (isMandatoryRule) {
        val count = courses.size
        val activities = if (count == 1) "1 attività obbligatoria" else "$count attività obbligatorie"
        val credits = selectedCredits.toInt()
        return if (credits > 0) "$activities · $credits CFU" else activities
    }
    val unitLabel = if (unit == ChoiceConstraintUnit.Activities) "attività" else "CFU"
    val min = minUnits?.toInt()?.takeIf { it > 0 }
    val max = maxUnits?.toInt()
    return when {
        // An optional rule is satisfied with zero picks, so its floor is effectively 0:
        // the min only kicks in once something is selected.
        isOptional && max != null -> "Fino a $max $unitLabel da scegliere"
        isOptional -> "Attività facoltative"
        min != null && max != null && min == max -> "$min $unitLabel da scegliere"
        min != null && max != null -> "Da $min a $max $unitLabel da scegliere"
        max != null -> "Fino a $max $unitLabel da scegliere"
        min != null -> "Almeno $min $unitLabel da scegliere"
        else -> "Attività a scelta"
    }
}

private fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."
    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto. Riprova."
}
