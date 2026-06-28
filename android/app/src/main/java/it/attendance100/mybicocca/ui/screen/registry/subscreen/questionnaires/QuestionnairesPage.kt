package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.navigation.scene.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.displayName
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.ext.pending
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.QuestionnaireCompilationPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.QuestionnaireCompilationViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.compilationWizardHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.compilation.state.QuestionnaireCompilationRequest
import it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires.subscreen.units.QuestionnaireUnitsPage
import kotlinx.coroutines.flow.collectLatest

/**
 * "Questionari" (VAL_DID course-evaluation surveys) as a single sheet entry, in the same
 * modal language as the percorso sheet: a pinned morphing header over a multi-level body
 * pager — activity list, the activity's units, the compilation wizard, the
 * leave-without-sending and definitive-send confirms pushed over the wizard, and a
 * result page. The whole flow, including compiling a questionnaire, happens inside the
 * one sheet. BottomSheetSceneStrategy owns the container; this keeps its own state
 * machine and morphing header. Swipes on the pages scroll their content, never the
 * sheet: the header above (and the drag handle) is the only swipe-to-dismiss surface.
 *
 * The ViewModel outlives the sheet (shell-scoped): re-opening shows the cached list
 * instantly while a background refresh is kicked, and any dismissal clears the selection
 * so the next open never starts on a stale units / compile page. The compilation target
 * is set when a unit's Compila is tapped and keys an entry-scoped wizard ViewModel, so a
 * new unit gets a fresh server session (Esse3 never resumes drafts); while the compiler
 * is up the header carries the wizard's per-step status. Mid-wizard a stray scrim tap
 * morphs onto the leave confirm via the scene's LocalSheetDismissControl — discarding
 * compiled answers silently is too destructive — and the sheet can't be swiped away,
 * while a result page is always freely dismissable. The leave confirm carries whether
 * the whole sheet should close after Esci; Esci discards the wizard's session so the
 * next open starts fresh.
 *
 * System back walks the pager up one level before dismissing the sheet; the wizard
 * registers its own deeper handler (internal step-back + exit confirm) that wins while
 * enabled, and the header's back routes through the back dispatcher so it shares the
 * exact system-back semantics. A confirmed submission shows a success result page and
 * refreshes both the list and the open activity's units so the row shows "Compilato";
 * submission failures land on an error result page. MissingAnswers events are no-ops
 * here — Avanti is disabled until the page's mandatory questions are answered, and
 * Conferma until the whole questionnaire is complete.
 */
@Composable
fun QuestionnairesPage(
    viewModel: QuestionnairesViewModel,
) {
    val data by viewModel.activities.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val selectedActivity by viewModel.selectedActivity.collectAsStateWithLifecycle()
    val activityDetail by viewModel.activityDetail.collectAsStateWithLifecycle()
    val detailStatus by viewModel.detailStatus.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }

    val activities = data.valueOrNull()
    val loaded = data is Loadable.Loaded
    val pendingCount = activities.orEmpty().count { it.status.pending }
    val activity = selectedActivity

    var compileRequest by remember { mutableStateOf<QuestionnaireCompilationRequest?>(null) }
    var pendingConfirm by remember { mutableStateOf<ConfirmIntent?>(null) }
    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }

    @Suppress("DEPRECATION") val compileViewModel = compileRequest?.let { request ->
        hiltViewModel<QuestionnaireCompilationViewModel, QuestionnaireCompilationViewModel.Factory>(
            key = "questionnaire_compile_${request.activityChoiceId}_${request.questionnaireId}_${request.tags}",
            creationCallback = { factory: QuestionnaireCompilationViewModel.Factory ->
                factory.create(request)
            },
        )
    }
    val compileHeader = compileViewModel?.let { compilationWizardHeader(it) }

    val control = LocalSheetDismissControl.current
    DisposableEffect(Unit) {
        onDispose { viewModel.dismissActivity() }
    }
    val resetAndDismiss = { control?.dismiss() }

    val page = when {
        outcome != null -> QPage.Result
        pendingConfirm == ConfirmIntent.Send -> QPage.ConfirmSend
        pendingConfirm != null -> QPage.ConfirmExit
        compileRequest != null -> QPage.Compile
        activity != null -> QPage.Units
        else -> QPage.Root
    }

    val compiling = rememberUpdatedState(compileRequest != null)
    val onConfirmDismiss = remember {
        {
            if (compiling.value) {
                pendingConfirm = ConfirmIntent.LeaveAndDismiss
                false
            } else {
                true
            }
        }
    }
    SideEffect {
        control?.gesturesEnabled = compileRequest == null || outcome != null
        control?.confirmDismiss = if (outcome != null) ({ true }) else onConfirmDismiss
    }

    run {
        val context = LocalContext.current
        LaunchedEffect(compileViewModel) {
            compileViewModel?.events?.collectLatest { event ->
                when (event) {
                    QuestionnaireCompilationEvent.Confirmed -> {
                        pendingConfirm = null
                        compileRequest = null
                        outcome =
                            SheetOutcome.Success(context.getString(R.string.questionnaire_success_message))
                        viewModel.refresh()
                        viewModel.retryDetail()
                    }

                    QuestionnaireCompilationEvent.MissingAnswers -> Unit

                    is QuestionnaireCompilationEvent.Failed ->
                        outcome = SheetOutcome.Error(
                            context.getString(R.string.questionnaire_error_title),
                            event.cause
                        )
                }
            }
        }

        val seekableState =
            remember { androidx.compose.animation.core.SeekableTransitionState(page) }
        val transition = androidx.compose.animation.core.rememberTransition(
            seekableState,
            label = "questionnaires_pages"
        )

        LaunchedEffect(page) {
            if (seekableState.targetState != page) {
                seekableState.animateTo(page)
            }
        }

        androidx.activity.compose.PredictiveBackHandler(enabled = page != QPage.Root) { progress ->
            try {
                val fallback = when (page) {
                    QPage.Result -> if (pendingConfirm == ConfirmIntent.Send) QPage.ConfirmSend else if (pendingConfirm != null) QPage.ConfirmExit else if (compileRequest != null) QPage.Compile else if (activity != null) QPage.Units else QPage.Root
                    QPage.ConfirmSend -> if (compileRequest != null) QPage.Compile else if (activity != null) QPage.Units else QPage.Root
                    QPage.ConfirmExit -> if (compileRequest != null) QPage.Compile else if (activity != null) QPage.Units else QPage.Root
                    QPage.Compile -> if (activity != null) QPage.Units else QPage.Root
                    QPage.Units -> QPage.Root
                    QPage.Root -> QPage.Root
                }
                progress.collect { event ->
                    seekableState.seekTo(event.progress, targetState = fallback)
                }
                seekableState.animateTo(fallback)
                when {
                    outcome != null -> outcome = null
                    pendingConfirm != null -> pendingConfirm = null
                    compileRequest != null -> {
                        compileViewModel?.reset()
                        compileRequest = null
                    }

                    else -> viewModel.dismissActivity()
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                seekableState.animateTo(page)
            }
        }
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        Column(modifier = Modifier.testTag(QuestionnairesTestTags.ROOT)) {
            SheetPagerHeader(
                depth = page.depth,
                title = when (page) {
                    QPage.Root -> stringResource(R.string.questionnaire_root_title)
                    QPage.Units -> activity?.displayName
                        ?: stringResource(R.string.questionnaire_root_title)

                    QPage.Compile -> compileHeader?.title
                        ?: stringResource(R.string.questionnaire_compile_title)

                    QPage.ConfirmExit -> stringResource(R.string.questionnaire_confirm_exit_title)
                    QPage.ConfirmSend -> stringResource(R.string.questionnaire_confirm_send_title)
                    QPage.Result -> ""
                },
                subtitle = when (page) {
                    QPage.Root -> rootSubtitle(loaded, pendingCount)
                    QPage.Units -> activityDetail.valueOrNull()?.questionnaireName?.let(::AnnotatedString)
                    QPage.Compile -> compileHeader?.subtitle
                    QPage.ConfirmExit, QPage.ConfirmSend ->
                        compileHeader?.title?.let(::AnnotatedString)
                    QPage.Result -> null
                },
                onBack = when (page) {
                    QPage.Root -> null
                    QPage.Units -> ({ viewModel.dismissActivity() })
                    QPage.Compile -> ({ backDispatcher?.onBackPressed() })
                    QPage.ConfirmExit, QPage.ConfirmSend -> ({ pendingConfirm = null })
                    QPage.Result -> null
                },
            )
            transition.AnimatedContent(
                modifier = Modifier.sheetBodyGestureBarrier(),
                transitionSpec = {
                    sheetPageTransform(forward = targetState.depth >= initialState.depth)
                },
                contentKey = { it.name },
            ) { target ->
                when (target) {
                    QPage.Root -> ActivitiesPage(
                        loaded = loaded,
                        activities = activities,
                        syncStatus = syncStatus,
                        onRetry = viewModel::refresh,
                        onOpenActivity = viewModel::openActivity,
                    )

                    QPage.Units -> Box(modifier = Modifier.testTag(QuestionnairesTestTags.UNITS_PAGE)) {
                        QuestionnaireUnitsPage(
                            detail = activityDetail,
                            detailStatus = detailStatus,
                            onCompileUnit = { detail, unit ->
                                val questionnaireId = detail.questionnaireId
                                val questionnaireConfigId = detail.questionnaireConfigId
                                if (questionnaireId != null && questionnaireConfigId != null) {
                                    compileRequest = QuestionnaireCompilationRequest(
                                        activityChoiceId = detail.activityChoiceId,
                                        questionnaireId = questionnaireId,
                                        questionnaireConfigId = questionnaireConfigId,
                                        anonymous = detail.anonymous,
                                        tags = unit.tags,
                                        activityName = activity?.displayName.orEmpty(),
                                        lecturerName = unit.lecturerName,
                                        partitionName = unit.partitionName,
                                    )
                                }
                            },
                            onRetry = viewModel::retryDetail,
                        )
                    }

                    QPage.Compile -> compileViewModel?.let { compileVm ->
                        QuestionnaireCompilationPage(
                            viewModel = compileVm,
                            onExitAttempt = { pendingConfirm = ConfirmIntent.Leave },
                            onConfirmAttempt = { pendingConfirm = ConfirmIntent.Send },
                        )
                    }

                    QPage.ConfirmExit -> ConfirmPage(
                        body = stringResource(R.string.questionnaire_confirm_exit_body),
                        confirmLabel = stringResource(R.string.questionnaire_confirm_exit_action),
                        onContinue = { pendingConfirm = null },
                        onConfirm = {
                            val intent = pendingConfirm
                            pendingConfirm = null
                            compileViewModel?.reset()
                            compileRequest = null
                            if (intent == ConfirmIntent.LeaveAndDismiss) resetAndDismiss()
                        },
                    )

                    QPage.ConfirmSend -> ConfirmPage(
                        body = stringResource(R.string.questionnaire_confirm_send_body),
                        confirmLabel = stringResource(R.string.questionnaire_confirm_send_action),
                        onContinue = { pendingConfirm = null },
                        onConfirm = { compileViewModel?.confirm() },
                    )

                    QPage.Result -> outcome?.let { current ->
                        SheetResultPage(outcome = current, onDismiss = { outcome = null })
                    }
                }
            }
        }
    }
}

/** Pages of the questionnaires sheet's state machine; [depth] drives the pager's slide direction. */
private enum class QPage(val depth: Int) {
    Root(0),
    Units(1),
    Compile(2),
    ConfirmExit(3),
    ConfirmSend(3),
    Result(4),
}

/** What the user was doing when a confirm page interrupted them. */
private enum class ConfirmIntent { Leave, LeaveAndDismiss, Send }

@Composable
private fun rootSubtitle(loaded: Boolean, pendingCount: Int): AnnotatedString? = when {
    !loaded -> null
    pendingCount == 0 -> AnnotatedString(stringResource(R.string.questionnaire_no_pending))
    else -> AnnotatedString(pluralStringResource(R.plurals.questionnaire_pending_count, pendingCount, pendingCount))
}

/**
 * Confirm page in the sheet's own language: body copy and the connected action pair —
 * Continua leads wide on the brand fill, the confirm action trails smaller and tonal.
 * Used for both the leave-without-sending and the definitive-send confirmations.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConfirmPage(
    body: String,
    confirmLabel: String,
    onContinue: () -> Unit,
    onConfirm: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val haptic = rememberHapticManager()

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilledTonalButton(
                onClick = { haptic.tap(); onConfirm() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Text(confirmLabel, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { haptic.tap(); onContinue() },
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandBg,
                    contentColor = brandFg,
                ),
            ) {
                Text(
                    stringResource(R.string.questionnaire_confirm_continue),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Root body states: an error message with retry when the first load failed, a loading
 * indicator (held for a minimum beat so quick fetches don't flash it), an empty message
 * when no course evaluation is open, and otherwise the activity list. Height changes are
 * animated so the modal never snaps to a new size as content lands.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActivitiesPage(
    loaded: Boolean,
    activities: List<QuestionnaireActivity>?,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onOpenActivity: (QuestionnaireActivity) -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && activities.isNullOrEmpty() -> {
                val haptic = rememberHapticManager()
                SheetMessage(
                    icon = Icons.Outlined.CloudOff,
                    title = stringResource(R.string.common_error_title),
                    body = failure.cause.friendlyMessage(),
                    action = { RetryButton(onClick = { haptic.tap(); onRetry() }) },
                    modifier = Modifier.testTag(QuestionnairesTestTags.ROOT_ERROR),
                )
            }

            !settled -> SheetLoadingIndicator(
                label = stringResource(R.string.questionnaire_loading),
                modifier = Modifier.testTag(QuestionnairesTestTags.ROOT_LOADING),
            )

            activities.isNullOrEmpty() -> SheetMessage(
                icon = Icons.AutoMirrored.Outlined.FactCheck,
                title = stringResource(R.string.questionnaire_no_activities),
                body = stringResource(R.string.questionnaire_no_activities_body),
                modifier = Modifier.testTag(QuestionnairesTestTags.ROOT_EMPTY),
            )

            else -> ActivityList(activities = activities, onOpenActivity = onOpenActivity)
        }
    }
}

/**
 * One connected stack, pending first and completed after: the status glyphs alone tell
 * the groups apart, with no section header tiles.
 */
@Composable
private fun ActivityList(
    activities: List<QuestionnaireActivity>,
    onOpenActivity: (QuestionnaireActivity) -> Unit,
) {
    val ordered = remember(activities) {
        activities.sortedBy { it.status == QuestionnaireActivityStatus.Completed }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp)
            .testTag(QuestionnairesTestTags.ACTIVITY_LIST),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(
            items = ordered,
            key = { _, activity -> activity.activityChoiceId },
        ) { index, activity ->
            val large = 20.dp
            val small = 4.dp
            QuestionnaireActivityRow(
                activity = activity,
                shape = RoundedCornerShape(
                    topStart = if (index == 0) large else small,
                    topEnd = if (index == 0) large else small,
                    bottomStart = if (index == ordered.lastIndex) large else small,
                    bottomEnd = if (index == ordered.lastIndex) large else small,
                ),
                onClick = { onOpenActivity(activity) },
                modifier = Modifier.testTag(QuestionnairesTestTags.activity(activity.activityChoiceId)),
            )
        }
    }
}

/**
 * One activity as a connected segment row: a status glyph chip (an expressive shape and
 * icon per status, on its status accent), the activity name with its code as caption,
 * and a trailing chevron into the units page.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuestionnaireActivityRow(
    activity: QuestionnaireActivity,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = rememberHapticManager()
    val accent = when (activity.status) {
        QuestionnaireActivityStatus.ToCompile -> scheme.primaryContainer to scheme.onPrimaryContainer
        QuestionnaireActivityStatus.PartiallyCompleted -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        QuestionnaireActivityStatus.Completed -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        QuestionnaireActivityStatus.ConfigurationError -> scheme.errorContainer to scheme.onErrorContainer
    }

    Surface(
        onClick = onClick,
        shape = shape,
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(activity.status.glyphShape())
                    .background(accent.first),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = activity.status.glyphIcon(),
                    contentDescription = null,
                    tint = accent.second,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                activity.activityCode?.takeIf { it.isNotBlank() }?.let { code ->
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** MaterialShapes getters are @Composable, so the mapper is too. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuestionnaireActivityStatus.glyphShape(): Shape = when (this) {
    QuestionnaireActivityStatus.ToCompile -> MaterialShapes.Cookie6Sided.toShape()
    QuestionnaireActivityStatus.PartiallyCompleted -> MaterialShapes.Clover4Leaf.toShape()
    QuestionnaireActivityStatus.Completed -> MaterialShapes.Circle.toShape()
    QuestionnaireActivityStatus.ConfigurationError -> MaterialShapes.Square.toShape()
}

private fun QuestionnaireActivityStatus.glyphIcon(): ImageVector = when (this) {
    QuestionnaireActivityStatus.ToCompile -> Icons.Rounded.EditNote
    QuestionnaireActivityStatus.PartiallyCompleted -> Icons.Rounded.HourglassBottom
    QuestionnaireActivityStatus.Completed -> Icons.Rounded.Check
    QuestionnaireActivityStatus.ConfigurationError -> Icons.Rounded.ErrorOutline
}
