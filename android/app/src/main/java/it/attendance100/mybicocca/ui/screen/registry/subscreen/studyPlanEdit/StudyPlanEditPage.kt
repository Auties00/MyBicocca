package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.studyplan.ChoiceConstraintUnit
import it.attendance100.mybicocca.domain.model.studyplan.EditableCourse
import it.attendance100.mybicocca.domain.model.studyplan.EditableRule
import it.attendance100.mybicocca.domain.model.studyplan.PlanApprovalType
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.ui.component.button.MorphKnob
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.StudyPlanEditViewModel.Companion.PATH_SEGMENT
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state.StudyPlanEditEvent
import it.attendance100.mybicocca.ui.screen.registry.theme.RegistryAccent
import it.attendance100.mybicocca.ui.screen.registry.theme.registryAccent
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Percorso-first plan compiler, hosted as a page inside the percorso sheet's pager: a
 * wizard whose first page picks the percorso (plan schema) when the regulation offers
 * any, followed by one button-driven page per course year with rule groups in the Piano
 * di Studi connected-segment style. The connected button pair at the bottom steps the
 * pager, morphing Avanti into Invia at the end. The ViewModel is hoisted by the hosting
 * sheet, which also owns the leave-without-submitting confirmation (onExitAttempt pushes
 * it) and surfaces the submit outcome as a result page — validity already lives in the
 * rule pills, so nothing is reported in-page.
 *
 * The first paint waits for the percorso lookup so the step list never reshuffles under
 * the user once the pager is on screen; when no options exist and nothing is selectable
 * from the request, the flow dead-ends with an error or lock message, while the
 * rules-only flow (percorso unico già noto) walks the usual loading / error / empty
 * states before the pager.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StudyPlanEditPage(
    viewModel: StudyPlanEditViewModel,
    onExitAttempt: () -> Unit,
    onSubmitted: (message: String) -> Unit,
    onSubmitFailed: (message: String) -> Unit,
) {
    val pathData by viewModel.path.collectAsStateWithLifecycle()
    val rulesData by viewModel.rules.collectAsStateWithLifecycle()
    val selectedSchemaId by viewModel.selectedSchemaId.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val currentSegment by viewModel.currentSegment.collectAsStateWithLifecycle()
    val submitting by viewModel.submitting.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is StudyPlanEditEvent.Submitted -> onSubmitted(event.message)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.submitError.collectLatest { message ->
            if (message != null) onSubmitFailed(message)
        }
    }

    val path = pathData.valueOrNull()
    val options = path?.options.orEmpty()
    val hasPathStep = options.isNotEmpty()
    val rules = rulesData.valueOrNull()

    when {
        pathData !is Loadable.Loaded ->
            SheetLoadingIndicator(label = stringResource(R.string.studyplanedit_loading_paths))

        !hasPathStep && selectedSchemaId == null -> when (val status = syncStatus) {
            is SyncStatus.Failed -> SheetError(cause = status.cause, onRetry = viewModel::refresh)
            else -> SheetMessage(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.studyplanedit_unavailable_title),
                body = stringResource(R.string.studyplanedit_unavailable_body),
            )
        }

        !hasPathStep && rules == null -> when (val status = syncStatus) {
            is SyncStatus.Failed -> SheetError(cause = status.cause, onRetry = viewModel::refresh)
            else -> SheetLoadingIndicator(label = stringResource(R.string.studyplanedit_loading_rules))
        }

        !hasPathStep && rules.orEmpty().isEmpty() -> SheetMessage(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.studyplanedit_no_rules_title),
            body = stringResource(R.string.studyplanedit_no_rules_body),
        )

        else -> PlanCompilerPager(
            options = options,
            selectedSchemaId = selectedSchemaId,
            rules = rules,
            rulesFailure = (syncStatus as? SyncStatus.Failed)?.cause?.takeIf { rules == null },
            initialSegment = currentSegment,
            submitting = submitting,
            schemaChanged = selectedSchemaId != null && path?.currentSchemaId != null &&
                selectedSchemaId != path.currentSchemaId,
            onSegmentSettled = viewModel::setSegment,
            onSelectSchema = viewModel::selectSchema,
            onLoadRules = viewModel::loadSelectedRules,
            onToggleCourse = viewModel::toggleCourse,
            onSubmit = viewModel::submit,
            onExitAttempt = onExitAttempt,
        )
    }
}

/**
 * What the hosting sheet's pinned header shows while the wizard is up: the current
 * step's question as the title, the rule's year and live status as the subtitle, with
 * the status rendered as inline colored text (success green when satisfied, warning
 * orange while not) like the other modals' state lines.
 */
data class EditWizardHeader(val title: String, val subtitle: AnnotatedString?)

/**
 * Builds the wizard's per-step header: "Scegli il percorso" with the mandatory status on
 * the percorso step, then per rule the year as title and the requirement in caps
 * followed by the live selection count colored by whether the rule currently holds.
 * Pre-pager states (loading, dead ends) keep the generic title and the fallback
 * subtitle.
 */
@Composable
fun editWizardHeader(
    viewModel: StudyPlanEditViewModel,
    fallbackSubtitle: String?,
): EditWizardHeader {
    val pathData by viewModel.path.collectAsStateWithLifecycle()
    val rulesData by viewModel.rules.collectAsStateWithLifecycle()
    val selectedSchemaId by viewModel.selectedSchemaId.collectAsStateWithLifecycle()
    val segment by viewModel.currentSegment.collectAsStateWithLifecycle()

    val fallback = EditWizardHeader(
        title = stringResource(R.string.studyplan_edit_path),
        subtitle = fallbackSubtitle?.let(::AnnotatedString),
    )
    if (pathData !is Loadable.Loaded) return fallback

    val options = pathData.valueOrNull()?.options.orEmpty()
    val rule = rulesData.valueOrNull().orEmpty().firstOrNull { it.choiceId == segment }
    val pathToChooseLabel = stringResource(R.string.studyplanedit_path_to_choose)
    val mandatoryLabel = stringResource(R.string.studyplanedit_mandatory_caps)

    return when {
        segment == PATH_SEGMENT && options.isNotEmpty() -> EditWizardHeader(
            title = stringResource(R.string.studyplanedit_choose_path),
            subtitle = buildAnnotatedString {
                append(pathToChooseLabel)
                withStyle(
                    SpanStyle(
                        color = ruleStatusColor(satisfied = selectedSchemaId != null),
                        fontWeight = FontWeight.SemiBold,
                    ),
                ) {
                    append(mandatoryLabel)
                }
            },
        )

        rule != null -> EditWizardHeader(
            title = ruleYearLabel(rule.courseYear),
            subtitle = buildAnnotatedString {
                append(rule.requirementLabel())
                append(" · ")
                withStyle(
                    SpanStyle(
                        color = ruleStatusColor(satisfied = rule.isSatisfied),
                        fontWeight = FontWeight.SemiBold,
                    ),
                ) {
                    append(rule.selectionLabel())
                }
            },
        )

        else -> fallback
    }
}

@Composable
private fun ruleYearLabel(year: Int): String =
    if (year == 0) {
        stringResource(R.string.studyplan_year_other_activities)
    } else {
        stringResource(R.string.studyplan_year_label, year)
    }

/**
 * Success green / warning orange for the inline rule status — neither exists as a
 * Material role, so they are fixed pairs picked per theme like the registry tones.
 */
@Composable
private fun ruleStatusColor(satisfied: Boolean): Color =
    if (satisfied) registryAccent(RegistryAccent.Success) else registryAccent(RegistryAccent.Warning)

/**
 * The rule's requirement in caps. A rule is a single flavour — Esse3's opzFlg (optional)
 * and tipo "O" (mandatory) are rule-level flags, so mandatory and optional units never
 * mix inside one rule. Units follow the rule's own measure (CFU or activity picks). An
 * optional rule is satisfied with zero picks — its floor only kicks in once something is
 * selected, so the label leads with the ceiling, never "TRA".
 */
@Composable
private fun EditableRule.requirementLabel(): String {
    val activities = unit == ChoiceConstraintUnit.Activities
    val unitName = stringResource(
        if (activities) R.string.studyplanedit_unit_activities else R.string.common_cfu,
    )
    if (isMandatoryRule) {
        return stringResource(
            R.string.studyplanedit_req_mandatory_credits,
            effectiveMinUnits?.toInt() ?: 0,
            unitName,
        )
    }

    val mandatoryAdj = stringResource(
        if (activities) {
            R.string.studyplanedit_mandatory_adj_feminine
        } else {
            R.string.studyplanedit_mandatory_adj_masculine
        },
    )
    val min = minUnits?.toInt()?.takeIf { it > 0 }
    val max = maxUnits?.toInt()
    return when {
        isOptional && min != null && max != null && min == max ->
            stringResource(R.string.studyplanedit_req_optional_exact, min, unitName)
        isOptional && max != null ->
            stringResource(R.string.studyplanedit_req_optional_up_to, max, unitName)
        isOptional -> stringResource(R.string.studyplanedit_req_optional, unitName)
        min != null && max != null && min == max ->
            stringResource(R.string.studyplanedit_req_exact, min, unitName, mandatoryAdj)
        min != null && max != null ->
            stringResource(R.string.studyplanedit_req_between, min, max, unitName, mandatoryAdj)
        max != null ->
            stringResource(R.string.studyplanedit_req_up_to, max, unitName, mandatoryAdj)
        min != null ->
            stringResource(R.string.studyplanedit_req_at_least, min, unitName, mandatoryAdj)
        else -> stringResource(R.string.studyplanedit_req_free_choice, unitName)
    }
}

/** The live selection count in the rule's own unit: "8/12 CFU SELEZIONATI". */
@Composable
private fun EditableRule.selectionLabel(): String {
    val activities = unit == ChoiceConstraintUnit.Activities
    val unitName = stringResource(
        if (activities) R.string.studyplanedit_unit_activities else R.string.common_cfu,
    )
    val selectedAdj = stringResource(
        if (activities) {
            R.string.studyplanedit_selected_adj_feminine
        } else {
            R.string.studyplanedit_selected_adj_masculine
        },
    )
    val selected = selectedUnits.toInt()
    val target = (effectiveMaxUnits ?: effectiveMinUnits)?.toInt()
    return if (target != null) {
        stringResource(R.string.studyplanedit_selection_of, selected, target, unitName, selectedAdj)
    } else {
        stringResource(R.string.studyplanedit_selection, selected, unitName, selectedAdj)
    }
}

/**
 * The wizard's pager and bottom bar. One question per page: each rule is its own step,
 * years ascending with the year-agnostic rules (courseYear == 0) closing the wizard, and
 * the percorso step, when present, always leading. The pages fill a fixed height cap so
 * the wizard never jumps per step, and only the bottom bar drives the pager — pages
 * don't react to swipes. An activity picked under any rule can't be picked again
 * elsewhere.
 *
 * The ViewModel's segment is kept as the survives-config-change record of where we are,
 * fed from targetPage (not settledPage) so the sheet header morphs in lockstep with the
 * slide. Rules are fetched lazily: Avanti on the percorso page triggers the load and the
 * advance lands once the rules (and therefore the year pages) exist; failure and empty
 * drop the pending advance and let the percorso page's inline rows tell the story, with
 * retry resuming the interrupted advance and a re-pick mid-load invalidating it. The
 * advance effect scrolls BEFORE dropping its pending flag — the flag keys the effect, so
 * flipping it first would restart the effect and cancel the animation mid-flight,
 * bouncing the pager back onto the percorso page.
 *
 * Avanti is gated on the visible rule (from the percorso page, just on a picked schema
 * whose rules didn't load empty); Invia on the whole plan. The percorso page is never
 * the submit page, even while it's the only one. System back walks the pager first;
 * leaving with unsent picks bubbles up to the hosting sheet, which pushes its confirm
 * page, and with nothing to lose the handler stays disabled so the sheet's own back
 * handling closes the editor directly.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlanCompilerPager(
    options: List<StudyPathOption>,
    selectedSchemaId: Long?,
    rules: List<EditableRule>?,
    rulesFailure: Throwable?,
    initialSegment: Long,
    submitting: Boolean,
    schemaChanged: Boolean,
    onSegmentSettled: (Long) -> Unit,
    onSelectSchema: (Long) -> Unit,
    onLoadRules: () -> Unit,
    onToggleCourse: (ruleChoiceId: Long, courseChoiceId: Long) -> Unit,
    onSubmit: () -> Unit,
    onExitAttempt: () -> Unit,
) {
    val hasPathStep = options.isNotEmpty()
    val orderedRules = remember(rules) {
        rules.orEmpty().sortedBy { if (it.courseYear == 0) Int.MAX_VALUE else it.courseYear }
    }
    val segments = remember(orderedRules, hasPathStep) {
        val ruleSegments = orderedRules.map { it.choiceId }
        if (hasPathStep) listOf(PATH_SEGMENT) + ruleSegments else ruleSegments
    }
    val loadedRules = rules.orEmpty()
    val isAllValid = remember(rules) { loadedRules.isNotEmpty() && loadedRules.all { it.isSatisfied } }
    val selectedCodes = remember(rules) {
        loadedRules.flatMap { rule -> rule.courses.filter { it.isSelected }.map { it.code } }.toSet()
    }
    val hasChanges = schemaChanged || remember(rules) {
        loadedRules.any { rule -> rule.courses.any { it.isSelected != it.isInitialSelected } }
    }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = segments.indexOf(initialSegment).coerceAtLeast(0),
    ) { segments.size }

    LaunchedEffect(pagerState, segments) {
        snapshotFlow { pagerState.targetPage }.collectLatest { page ->
            segments.getOrNull(page)?.let(onSegmentSettled)
        }
    }

    var pendingAdvance by remember { mutableStateOf(false) }
    LaunchedEffect(pendingAdvance, rules, rulesFailure) {
        if (!pendingAdvance) return@LaunchedEffect
        when {
            rulesFailure != null -> pendingAdvance = false
            rules != null -> {
                if (rules.isNotEmpty()) pagerState.animateScrollToPage(1)
                pendingAdvance = false
            }
        }
    }

    BackHandler(enabled = pagerState.currentPage > 0 || (hasChanges && !submitting)) {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else {
            onExitAttempt()
        }
    }

    Column(Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            key = { segments[it] },
            userScrollEnabled = false,
        ) { page ->
            when (val segment = segments[page]) {
                PATH_SEGMENT -> PathChoicePage(
                    options = options,
                    selectedSchemaId = selectedSchemaId,
                    rulesEmpty = selectedSchemaId != null && rules != null && rules.isEmpty(),
                    rulesFailure = rulesFailure,
                    onRetryRules = {
                        pendingAdvance = true
                        onLoadRules()
                    },
                    onSelect = { schemaId ->
                        pendingAdvance = false
                        onSelectSchema(schemaId)
                    },
                )

                else -> {
                    val rule = orderedRules.firstOrNull { it.choiceId == segment }
                    if (rule != null) {
                        RulePage(
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
        }

        val currentSegment = segments.getOrNull(pagerState.currentPage)
        val onPathPage = currentSegment == PATH_SEGMENT
        val canAdvance = if (onPathPage) {
            selectedSchemaId != null && rules?.isEmpty() != true
        } else {
            loadedRules.firstOrNull { it.choiceId == currentSegment }?.isSatisfied ?: true
        }

        WizardBottomBar(
            pageCount = segments.size,
            currentPage = pagerState.currentPage,
            isLast = !onPathPage && pagerState.currentPage == segments.lastIndex,
            canAdvance = canAdvance,
            canSubmit = isAllValid && !submitting,
            advancing = pendingAdvance,
            submitting = submitting,
            onBack = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            },
            onNext = {
                if (onPathPage && rules == null) {
                    pendingAdvance = true
                    onLoadRules()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            onSubmit = onSubmit,
        )
    }
}

/**
 * Step zero, headerless: the question ("Scegli il percorso" + the Obbligatorio status)
 * lives in the sheet's pinned header, so the page is just the connected option tiles —
 * one per distinct percorso/orientamento/profilo/part-time tuple, knob-selected like the
 * course tiles. Nothing arrives preselected: the pick is a deliberate, mandatory act and
 * Avanti stays disabled until it's made. Inline status rows close the list when the
 * selected schema's rules failed to load (with retry) or loaded empty.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PathChoicePage(
    options: List<StudyPathOption>,
    selectedSchemaId: Long?,
    rulesEmpty: Boolean,
    rulesFailure: Throwable?,
    onRetryRules: () -> Unit,
    onSelect: (Long) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(options, key = { _, option -> option.schemaId }) { index, option ->
            PathChoiceCard(
                option = option,
                selected = option.schemaId == selectedSchemaId,
                isFirst = index == 0,
                isLast = index == options.lastIndex,
                onClick = { onSelect(option.schemaId) },
            )
        }

        when {
            rulesFailure != null -> item(key = "rules_failure") {
                InlineStatusRow {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = scheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.studyplanedit_rules_not_loaded),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.height(16.dp))
                    val haptic = rememberHapticManager()
                    TextButton(onClick = { haptic.tap(); onRetryRules() }) { Text(stringResource(R.string.common_retry)) }
                }
            }

            rulesEmpty -> item(key = "rules_empty") {
                InlineStatusRow {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.studyplanedit_path_no_rules),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineStatusRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/**
 * One percorso option as a connected knob-selected tile: the title with the
 * orientamento/profilo/part-time/language facets beneath, the approval flavour as a
 * caption, and the schema's access-condition note when present — a server-side condition
 * that can't be verified client-side, surfaced so a rejected submit doesn't come out of
 * nowhere.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PathChoiceCard(
    option: StudyPathOption,
    selected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = if (isFirst) large else small,
            topEnd = if (isFirst) large else small,
            bottomStart = if (isLast) large else small,
            bottomEnd = if (isLast) large else small,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val detail = buildList {
                    option.orientamento?.label?.takeIf { it.isNotBlank() }?.let(::add)
                    option.profilo?.label?.takeIf { it.isNotBlank() }?.let(::add)
                    option.partTime?.label?.takeIf { it.isNotBlank() }?.let(::add)
                    addAll(option.languages)
                }.joinToString(" · ")
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val approvalLabel = when (option.approval) {
                    PlanApprovalType.Automatic -> stringResource(R.string.studyplanedit_approval_automatic)
                    PlanApprovalType.Manual -> stringResource(R.string.studyplanedit_approval_manual)
                    PlanApprovalType.AutomaticIfCompliant ->
                        stringResource(R.string.studyplanedit_approval_automatic_if_compliant)
                    PlanApprovalType.Unknown -> null
                }
                if (approvalLabel != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = approvalLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                option.conditionNote?.let { note ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            MorphKnob(checked = selected)
        }
    }
}

/**
 * One rule's activities as a single connected segment group: the question itself (rule
 * description, year, live status) lives in the sheet's pinned header, so the page is
 * headerless. Optional pre/post notes cap the group. Mandatory rules render the same
 * way, just without interaction.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RulePage(
    rule: EditableRule,
    onToggleCourse: (Long) -> Unit,
    isCourseEnabled: (EditableCourse) -> Boolean,
) {
    val locked = rule.isMandatoryRule
    val hasPreNote = !rule.preNote.isNullOrBlank()
    val hasPostNote = !rule.postNote.isNullOrBlank()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (hasPreNote) {
            item(key = "pre_note") {
                NoteTile(text = rule.preNote, capTop = true, capBottom = false)
            }
        }
        itemsIndexed(rule.courses, key = { _, course -> course.choiceId }) { index, course ->
            val haptic = rememberHapticManager()
            CourseTile(
                course = course,
                enabled = !locked && isCourseEnabled(course),
                locked = locked,
                isFirst = index == 0 && !hasPreNote,
                isLast = index == rule.courses.lastIndex && !hasPostNote,
                onToggle = { haptic.tap(); onToggleCourse(course.choiceId) },
            )
        }
        if (hasPostNote) {
            item(key = "post_note") {
                NoteTile(text = rule.postNote, capTop = false, capBottom = true)
            }
        }
    }
}

/**
 * A connected course tile in the Piano di Studi style: leading CFU chip, name with the
 * course code as caption, and a trailing knob that morphs circle-to-sunny when picked.
 * Selection lives entirely in the chip (washing to the brand red with explicit white
 * content) and the knob — the tile itself doesn't change.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CourseTile(
    course: EditableCourse,
    enabled: Boolean,
    locked: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val selected = course.isSelected
    val checked = selected || locked || course.isMandatory
    val interactive = !locked && enabled && !course.isMandatory

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
            topStart = if (isFirst) large else small,
            topEnd = if (isFirst) large else small,
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
                        text = stringResource(R.string.common_cfu),
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
private fun NoteTile(text: String, capTop: Boolean, capBottom: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurfaceVariant,
        shape = RoundedCornerShape(
            topStart = if (capTop) 20.dp else 4.dp,
            topEnd = if (capTop) 20.dp else 4.dp,
            bottomStart = if (capBottom) 20.dp else 4.dp,
            bottomEnd = if (capBottom) 20.dp else 4.dp,
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * The app's connected button pair (see EventDetailSheet/ExamResultsPage): an icon-only
 * tonal back that springs in past the first page, and one persistent brand-filled
 * primary action (red in light, primaryContainer in dark — the shared CTA scheme) whose
 * label and icon slide between the Avanti / Invia / in-flight states, flattening its
 * start corners when the back button joins the pair. Broken rules disable the primary
 * action. Sheet-hosted, so the modal already clears the navigation bar.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WizardBottomBar(
    pageCount: Int,
    currentPage: Int,
    isLast: Boolean,
    canAdvance: Boolean,
    canSubmit: Boolean,
    advancing: Boolean,
    submitting: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val motion = MaterialTheme.motionScheme
    val spatialOffsetSpec = motion.defaultSpatialSpec<IntOffset>()
    val effectsFloatSpec = motion.defaultEffectsSpec<Float>()
    val backVisible = currentPage > 0 && !submitting

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
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
                val haptic = rememberHapticManager()
                FilledTonalButton(
                    onClick = { haptic.tap(); onBack() },
                    modifier = Modifier
                        .width(64.dp)
                        .height(56.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_prev_page),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            val startCorner by animateDpAsState(
                targetValue = if (backVisible) 8.dp else 28.dp,
                animationSpec = motion.defaultSpatialSpec(),
                label = "actionCorner",
            )
            val action = when {
                isLast && submitting -> WizardAction.Sending
                isLast -> WizardAction.Submit
                advancing -> WizardAction.Loading
                else -> WizardAction.Next
            }
            val haptic = rememberHapticManager()
            Button(
                onClick = {
                    haptic.tap()
                    when (action) {
                        WizardAction.Next -> onNext()
                        WizardAction.Submit -> onSubmit()
                        WizardAction.Loading, WizardAction.Sending -> Unit
                    }
                },
                enabled = if (isLast) (canSubmit && LocalIsOnline.current) || submitting else canAdvance || advancing,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(
                    topStart = startCorner,
                    bottomStart = startCorner,
                    topEnd = 28.dp,
                    bottomEnd = 28.dp,
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandBg,
                    contentColor = brandFg,
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
                                Text(
                                    stringResource(R.string.common_next),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            WizardAction.Submit -> {
                                Text(
                                    stringResource(R.string.studyplanedit_submit_plan),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            WizardAction.Loading -> {
                                LoadingIndicator(modifier = Modifier.size(24.dp), color = brandFg)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.common_loading),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            WizardAction.Sending -> {
                                LoadingIndicator(modifier = Modifier.size(24.dp), color = brandFg)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.studyplanedit_sending),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class WizardAction { Next, Loading, Submit, Sending }

/** Page indicator: the active dot stretches into a primary pill on spring physics. */
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
private fun SheetError(cause: Throwable, onRetry: () -> Unit) {
    SheetMessage(
        icon = Icons.Default.Warning,
        title = stringResource(R.string.common_load_failed),
        body = cause.friendlyMessage(),
        action = {
            val haptic = rememberHapticManager(); RetryButton(onClick = { haptic.tap(); onRetry() })
        },
    )
}
