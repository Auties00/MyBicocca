package it.attendance100.mybicocca.ui.screen.registry.subscreen.questionnaires

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivity
import it.attendance100.mybicocca.domain.model.questionnaire.QuestionnaireActivityStatus
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
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

// "Questionari" as a modal sheet, same modal language as the percorso sheet: a pinned
// morphing header over a multi-level body pager (activity list -> the activity's units ->
// the compilation wizard), with the leave-without-sending and definitive-send confirms
// pushed over the wizard like the percorso sheet's "uscire senza inviare?". The whole
// flow — including compiling a questionnaire — happens inside the one sheet now.
// Questionari as a single sheet entry: BottomSheetSceneStrategy owns the container; this keeps
// its own multi-level state machine (activities -> units -> the compilation wizard, with leave /
// send confirms) and morphing header. Mid-wizard it locks the sheet and morphs a dismissal into
// the leave-confirm via the scene's LocalSheetDismissControl.
@Composable
fun QuestionnairesPage(
    viewModel: QuestionnairesViewModel,
) {
    val data by viewModel.activities.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val selectedActivity by viewModel.selectedActivity.collectAsStateWithLifecycle()
    val activityDetail by viewModel.activityDetail.collectAsStateWithLifecycle()
    val detailStatus by viewModel.detailStatus.collectAsStateWithLifecycle()

    // The VM outlives the sheet (it is shell-scoped, not route-scoped): re-opening shows
    // the cached list instantly while this kicks a background refresh.
    LaunchedEffect(viewModel) { viewModel.refresh() }

    val activities = data.valueOrNull()
    val loaded = data is Loadable.Loaded
    val pendingCount = activities.orEmpty().count { it.status.pending }
    val activity = selectedActivity

    // The compilation target, set when a unit's Compila is tapped. Keyed per target so a
    // new unit gets a fresh server session (Esse3 never resumes drafts).
    var compileRequest by remember { mutableStateOf<QuestionnaireCompilationRequest?>(null) }
    // Non-null while a confirm page is up; carries whether the user is leaving or sending,
    // and (for a leaving intent) whether the whole sheet should close afterwards.
    var pendingConfirm by remember { mutableStateOf<ConfirmIntent?>(null) }
    // The outcome of a compile submission, shown as a dedicated result page instead of a snackbar.
    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }

    val compileViewModel = compileRequest?.let { request ->
        hiltViewModel<QuestionnaireCompilationViewModel, QuestionnaireCompilationViewModel.Factory>(
            key = "questionnaire_compile_${request.activityChoiceId}_${request.questionnaireId}_${request.tags}",
            creationCallback = { factory: QuestionnaireCompilationViewModel.Factory ->
                factory.create(request)
            },
        )
    }
    // While the compiler is up the header carries the wizard's per-step status (unit +
    // page + answered count) instead of the activity name.
    val compileHeader = compileViewModel?.let { compilationWizardHeader(it) }

    // Any dismissal resets to the root page so the next open never starts on a stale units /
    // compile page: the shell-scoped VM's selection is cleared when the entry leaves, and the
    // compile target (this sheet's own state) and its entry-scoped VM go with the entry.
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

    // Mid-wizard a stray scrim tap morphs onto the leave confirm instead of closing the whole
    // sheet — discarding compiled answers silently is too destructive — and the sheet can't be
    // swiped away. Driven through the scene's dismiss control; the back gesture is handled by
    // the BackHandler below.
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
        // A result page is always freely dismissable (the operation is over); only the live
        // wizard locks the sheet behind the leave confirm.
        control?.gesturesEnabled = compileRequest == null || outcome != null
        control?.confirmDismiss = if (outcome != null) ({ true }) else onConfirmDismiss
    }

    run {
        // The compiler's one-shot events (was handled by its full-screen host before).
        LaunchedEffect(compileViewModel) {
            compileViewModel?.events?.collectLatest { event ->
                when (event) {
                    QuestionnaireCompilationEvent.Confirmed -> {
                        pendingConfirm = null
                        compileRequest = null
                        outcome = SheetOutcome.Success("Questionario inviato. Grazie!")
                        // The submission flipped this unit's server-side state; refresh the
                        // list and the open activity's units so the row shows "Compilato".
                        viewModel.refresh()
                        viewModel.retryDetail()
                    }

                    // Unreachable in normal flow: Avanti is disabled until the page's mandatory
                    // questions are answered, and Conferma until the whole questionnaire is complete.
                    QuestionnaireCompilationEvent.MissingAnswers -> Unit

                    is QuestionnaireCompilationEvent.Failed ->
                        outcome = SheetOutcome.Error("Operazione non riuscita", event.cause)
                }
            }
        }

        // System back walks the pager up one level before dismissing the sheet. The
        // wizard registers its own deeper handler (internal step-back + exit confirm),
        // which wins while enabled; this one closes the compiler/units once that lets go.
        BackHandler(enabled = page != QPage.Root) {
            when {
                outcome != null -> outcome = null
                pendingConfirm != null -> pendingConfirm = null
                compileRequest != null -> {
                    // Only reached when the wizard has nothing to lose (its own handler
                    // is disabled); still discard the session for the next open.
                    compileViewModel?.reset()
                    compileRequest = null
                }

                else -> viewModel.dismissActivity()
            }
        }
        // Routing the header's back through the dispatcher gives it the exact system-back
        // semantics on the wizard page: step back first, leave-confirm when needed.
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        Column {
            SheetPagerHeader(
                depth = page.depth,
                title = when (page) {
                    QPage.Root -> "Questionari"
                    QPage.Units -> activity?.displayName ?: "Questionari"
                    QPage.Compile -> compileHeader?.title ?: "Questionario"
                    QPage.ConfirmExit -> "Uscire senza inviare?"
                    QPage.ConfirmSend -> "Confermare il questionario?"
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
            AnimatedContent(
                targetState = page,
                // Swipes on the pages scroll their content, never the sheet: the header
                // above (and the drag handle) is the only swipe-to-dismiss surface.
                modifier = Modifier.sheetBodyGestureBarrier(),
                transitionSpec = {
                    sheetPageTransform(forward = targetState.depth >= initialState.depth)
                },
                contentKey = { it.name },
                label = "questionnaires_pages",
            ) { target ->
                when (target) {
                    QPage.Root -> ActivitiesPage(
                        loaded = loaded,
                        activities = activities,
                        syncStatus = syncStatus,
                        onRetry = viewModel::refresh,
                        onOpenActivity = viewModel::openActivity,
                    )

                    QPage.Units -> QuestionnaireUnitsPage(
                        detail = activityDetail,
                        detailStatus = detailStatus,
                        onCompileUnit = { detail, unit ->
                            val questionnaireId = detail.questionnaireId
                            val questionnaireConfigId = detail.questionnaireConfigId
                            // The units page only offers Compila when both ids are present;
                            // re-checked here to keep the request fields non-null.
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

                    QPage.Compile -> compileViewModel?.let { compileVm ->
                        QuestionnaireCompilationPage(
                            viewModel = compileVm,
                            onExitAttempt = { pendingConfirm = ConfirmIntent.Leave },
                            onConfirmAttempt = { pendingConfirm = ConfirmIntent.Send },
                        )
                    }

                    QPage.ConfirmExit -> ConfirmPage(
                        body = "Le risposte date finora non sono state inviate e andranno perse.",
                        confirmLabel = "Esci",
                        onContinue = { pendingConfirm = null },
                        onConfirm = {
                            val intent = pendingConfirm
                            pendingConfirm = null
                            // Esci means discard: drop the wizard's session before leaving
                            // so the next open starts fresh.
                            compileViewModel?.reset()
                            compileRequest = null
                            if (intent == ConfirmIntent.LeaveAndDismiss) resetAndDismiss()
                        },
                    )

                    QPage.ConfirmSend -> ConfirmPage(
                        body = "La conferma è definitiva: le risposte non potranno più essere modificate.",
                        confirmLabel = "Conferma",
                        onContinue = { pendingConfirm = null },
                        // The Confirmed event clears pendingConfirm and the compile target.
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

private enum class QPage(val depth: Int) {
    Root(0),
    Units(1),
    Compile(2),
    ConfirmExit(3),
    ConfirmSend(3),
    Result(4),
}

// What the user was doing when a confirm page interrupted them.
private enum class ConfirmIntent { Leave, LeaveAndDismiss, Send }

private fun rootSubtitle(loaded: Boolean, pendingCount: Int): AnnotatedString? = when {
    !loaded -> null
    pendingCount == 0 -> AnnotatedString("Nessuna valutazione in attesa")
    pendingCount == 1 -> AnnotatedString("1 insegnamento in attesa di valutazione")
    else -> AnnotatedString("$pendingCount insegnamenti in attesa di valutazione")
}

// Confirm page in the sheet's own language: body copy and the connected action pair —
// Continua leads wide on the brand fill, the confirm action trails smaller and tonal.
// Used for both the leave-without-sending and the definitive-send confirmations.
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
                onClick = onConfirm,
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
                onClick = onContinue,
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandBg,
                    contentColor = brandFg,
                ),
            ) {
                Text("Continua", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

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
    // Hold the loading state for a beat so quick fetches don't flash it.
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    // The sheet only grows/shrinks vertically as content lands — animate the height change
    // here instead of letting the modal snap to the new size.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && activities.isNullOrEmpty() -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Caricamento non riuscito",
                body = failure.cause.friendlyMessage(),
                action = { RetryButton(onClick = onRetry) },
            )

            !settled -> SheetLoadingIndicator(label = "Caricamento questionari…")

            activities.isNullOrEmpty() -> SheetMessage(
                icon = Icons.AutoMirrored.Outlined.FactCheck,
                title = "Nessun questionario",
                body = "Quando un insegnamento aprirà la valutazione della didattica lo troverai qui.",
            )

            else -> ActivityList(activities = activities, onOpenActivity = onOpenActivity)
        }
    }
}

// One connected stack, pending first and completed after: the status glyphs already
// tell the groups apart, so the old "Da compilare"/"Compilati" header tiles are gone.
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
            .padding(horizontal = 16.dp),
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
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuestionnaireActivityRow(
    activity: QuestionnaireActivity,
    shape: Shape,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
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
        modifier = Modifier.fillMaxWidth(),
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

// MaterialShapes getters are @Composable in this material3 version, so the mapper is too.
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
