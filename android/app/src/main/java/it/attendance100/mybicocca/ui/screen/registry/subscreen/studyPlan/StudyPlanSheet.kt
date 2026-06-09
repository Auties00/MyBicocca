package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Print
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanType
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.state.StudyPlanEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.StudyPlanEditPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.StudyPlanEditViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.editWizardHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlanEdit.state.StudyPlanEditRequest
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import kotlinx.coroutines.flow.collectLatest

// Percorso e piano as a modal sheet, same modal language as the maps "Edifici" sheet: a
// pinned morphing header over a two-level body pager (year list -> year courses, or the
// plan-compiler wizard), with a connected modifica/stampa action footer instead of an
// in-page FAB.
// Percorso e piano as a single sheet entry: BottomSheetSceneStrategy owns the container; this
// keeps its own state machine (year list -> year courses, plus the plan-compiler wizard with its
// leave-confirm) and morphing header. Mid-wizard it locks the sheet and morphs a dismissal into
// the "uscire senza inviare?" confirm via the scene's LocalSheetDismissControl.
@Composable
fun StudyPlanPage(
    viewModel: StudyPlanViewModel,
) {
    val planData by viewModel.plan.collectAsStateWithLifecycle()
    val pathData by viewModel.studyPath.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val editable by viewModel.editable.collectAsStateWithLifecycle()
    val actionInProgress by viewModel.actionInProgress.collectAsStateWithLifecycle()
    val activeCareerId by viewModel.activeCareerId.collectAsStateWithLifecycle()

    // The VM outlives the sheet (it is shell-scoped, not route-scoped): re-opening shows the
    // cached snapshot instantly while this kicks a background refresh, like the old route push.
    LaunchedEffect(viewModel) { viewModel.refresh() }

    val plan = planData.valueOrNull()
    val path = pathData.valueOrNull()
    // Header waits for BOTH streams (fetched concurrently in the VM) so the subtitle
    // lands once, complete, instead of growing when the path arrives a moment later.
    val loaded = planData is Loadable.Loaded && pathData is Loadable.Loaded

    // Same availability rule as the old FAB: compilation window open and the ids
    // resolved (the path lookup alone is enough when no plan exists yet).
    val regulationId = plan?.choiceRegulationId ?: path?.choiceRegulationId
    val studentId = plan?.studentId ?: activeCareerId?.value
    val editAvailable = editable && regulationId != null && studentId != null

    // Years ascending, with the generic "no specific year" bucket at the end.
    val coursesByYear = remember(plan) {
        plan?.courses.orEmpty().groupBy { it.year }
            .toSortedMap(compareBy { if (it == StudyYear.Unknown) Int.MAX_VALUE else it.value })
    }
    var selectedYear by remember { mutableStateOf<StudyYear?>(null) }
    var editRequest by remember { mutableStateOf<StudyPlanEditRequest?>(null) }
    // Non-null while the leave-without-submitting confirm page is up; carries what the
    // user was trying to do so Esci can finish that exact intent.
    var pendingExit by remember { mutableStateOf<ExitIntent?>(null) }
    // The outcome of a print/submit, shown as a dedicated result page instead of a snackbar.
    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    // True when the outcome ends the edit flow (a submit) — its Chiudi/Fine closes the whole
    // sheet and the modal is freely dismissable. Print feedback is non-terminal (returns here).
    var outcomeTerminal by remember { mutableStateOf(false) }
    // The wizard's VM is hoisted here so the sheet can ask hasChanges() when a dismiss
    // gesture lands. Keyed per compilation target: a new plan/schema gets a fresh VM.
    val editViewModel = editRequest?.let { request ->
        hiltViewModel<StudyPlanEditViewModel, StudyPlanEditViewModel.Factory>(
            key = "plan_edit_${request.choiceRegulationId}_${request.schemaId}_${request.planId}",
            creationCallback = { factory: StudyPlanEditViewModel.Factory ->
                factory.create(request)
            },
        )
    }
    // While the editor page is up the header carries the wizard's per-step question
    // (percorso pick / one rule at a time) instead of a static "Modifica percorso".
    val editHeader = editViewModel?.let {
        editWizardHeader(it, fallbackSubtitle = if (loaded) headerSubtitle(plan, path) else null)
    }
    // A refresh can reshape the plan under an open year page; an evicted year falls
    // back to the list rather than rendering an empty sub-page.
    val activeYear = selectedYear?.takeIf { coursesByYear.containsKey(it) }
    val page = when {
        outcome != null -> PlanSheetPage.Result
        pendingExit != null -> PlanSheetPage.ConfirmExit
        editRequest != null -> PlanSheetPage.Edit
        activeYear != null -> PlanSheetPage.Year(activeYear)
        else -> PlanSheetPage.Root
    }

    // Any outside-dismissal attempt (scrim tap, stray gesture) mid-wizard morphs onto the Esci
    // confirm instead of dismissing — closing the whole sheet from a stray tap is too destructive
    // to do silently. Mid-wizard the sheet also can't be swiped away. Driven through the scene's
    // dismiss control; the back gesture is handled by the BackHandler below.
    val control = LocalSheetDismissControl.current
    val editing = rememberUpdatedState(editRequest != null)
    val onConfirmDismiss = remember {
        {
            if (editing.value) {
                pendingExit = ExitIntent.DismissSheet
                false
            } else {
                true
            }
        }
    }
    SideEffect {
        // A result page is always freely dismissable (the operation is over); only the live
        // wizard locks the sheet behind the Esci confirm.
        control?.gesturesEnabled = editRequest == null || outcome != null
        control?.confirmDismiss = if (outcome != null) ({ true }) else onConfirmDismiss
    }

    run {
        val context = LocalContext.current
        LaunchedEffect(viewModel) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is StudyPlanEvent.OpenPdf ->
                        runCatching { openPdfDocument(context, event.bytes, event.fileName) }
                            .onFailure {
                                outcomeTerminal = false
                                outcome = SheetOutcome.Error("Impossibile aprire il documento", it)
                            }
                    is StudyPlanEvent.ShowMessage -> {
                        outcomeTerminal = false
                        outcome = SheetOutcome.Info(event.message)
                    }
                }
            }
        }

        // System back walks the pager up one level before dismissing the sheet. The
        // wizard registers its own deeper handler (internal step-back + exit confirm),
        // which wins while enabled; this one closes the editor once that lets go.
        BackHandler(enabled = page != PlanSheetPage.Root) {
            when {
                outcome != null -> outcome = null
                pendingExit != null -> pendingExit = null
                editRequest != null -> {
                    // Only reached when the wizard has nothing to lose (its own handler
                    // is disabled); still discard segment/draft state for the next open.
                    editViewModel?.reset()
                    editRequest = null
                }

                else -> selectedYear = null
            }
        }
        // Routing the header's back through the dispatcher gives it the exact system-back
        // semantics on the wizard page: step back first, discard prompt when needed.
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        Column {
            SheetPagerHeader(
                depth = when (page) {
                    PlanSheetPage.Root -> 0
                    is PlanSheetPage.Year, PlanSheetPage.Edit -> 1
                    PlanSheetPage.ConfirmExit -> 2
                    PlanSheetPage.Result -> 1
                },
                title = when (page) {
                    PlanSheetPage.Root -> "Percorso"
                    is PlanSheetPage.Year -> page.year.label()
                    PlanSheetPage.Edit -> editHeader?.title ?: "Modifica percorso"
                    PlanSheetPage.ConfirmExit -> "Uscire senza inviare?"
                    PlanSheetPage.Result -> ""
                },
                subtitle = when (page) {
                    PlanSheetPage.Root -> if (loaded) headerSubtitle(plan, path) else null
                    is PlanSheetPage.Year -> yearSummary(coursesByYear[page.year].orEmpty())
                    PlanSheetPage.Edit -> editHeader?.subtitle
                    PlanSheetPage.ConfirmExit -> "Modifica percorso"
                    PlanSheetPage.Result -> null
                },
                onBack = when (page) {
                    PlanSheetPage.Root -> null
                    is PlanSheetPage.Year -> ({ selectedYear = null })
                    PlanSheetPage.Edit -> ({ backDispatcher?.onBackPressed() })
                    PlanSheetPage.ConfirmExit -> ({ pendingExit = null })
                    PlanSheetPage.Result -> null
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
                contentKey = { target ->
                    when (target) {
                        PlanSheetPage.Root -> "root"
                        is PlanSheetPage.Year -> "year_${target.year.value}"
                        PlanSheetPage.Edit -> "edit"
                        PlanSheetPage.ConfirmExit -> "confirm_exit"
                        PlanSheetPage.Result -> "result"
                    }
                },
                label = "study_plan_pages",
            ) { target ->
                when (target) {
                    PlanSheetPage.Root -> SheetBody(
                        loaded = loaded,
                        plan = plan,
                        coursesByYear = coursesByYear,
                        syncStatus = syncStatus,
                        onRetry = viewModel::refresh,
                        onYearClick = { selectedYear = it },
                        editAvailable = editAvailable,
                        onEdit = {
                            if (regulationId != null && studentId != null) {
                                editRequest = StudyPlanEditRequest(
                                    studentId = studentId,
                                    choiceRegulationId = regulationId,
                                    schemaId = plan?.schemaId ?: path?.currentSchemaId ?: 0L,
                                    planId = plan?.planId ?: 0L,
                                )
                            }
                        },
                        printing = actionInProgress,
                        onPrint = viewModel::printPlan,
                    )

                    is PlanSheetPage.Year -> YearCoursesPage(
                        courses = coursesByYear[target.year].orEmpty(),
                    )

                    PlanSheetPage.Edit -> editViewModel?.let { editVm ->
                        StudyPlanEditPage(
                            viewModel = editVm,
                            onExitAttempt = { pendingExit = ExitIntent.CloseEditor },
                            onSubmitted = { message ->
                                pendingExit = null
                                editRequest = null
                                outcomeTerminal = true
                                outcome = SheetOutcome.Success(message)
                                // The submit changed the plan server-side; the root page
                                // re-shows with fresh data.
                                viewModel.refresh()
                            },
                            // Keep the wizard so Riprova returns to it; Chiudi closes the sheet.
                            onSubmitFailed = { message ->
                                outcomeTerminal = true
                                outcome = SheetOutcome.Error("Invio non riuscito", body = message)
                            },
                        )
                    }

                    PlanSheetPage.ConfirmExit -> DiscardChangesPage(
                        onContinue = { pendingExit = null },
                        onExit = {
                            val intent = pendingExit
                            pendingExit = null
                            // Esci means discard: drop the wizard's progress before
                            // leaving so the next open starts fresh.
                            editViewModel?.reset()
                            editRequest = null
                            // Finish what triggered the confirm: closing just the wizard,
                            // or the whole sheet.
                            if (intent == ExitIntent.DismissSheet) control?.dismiss()
                        },
                    )

                    PlanSheetPage.Result -> outcome?.let { current ->
                        SheetResultPage(
                            outcome = current,
                            // A terminal (submit) outcome closes the whole sheet; print feedback
                            // just returns to the percorso.
                            onDismiss = { if (outcomeTerminal) control?.dismiss() else outcome = null },
                            // A failed submit keeps the wizard, so Riprova returns to it to resend.
                            onRetry = if (outcomeTerminal && current is SheetOutcome.Error && editRequest != null) {
                                { outcome = null }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}

private sealed interface PlanSheetPage {
    val depth: Int

    data object Root : PlanSheetPage {
        override val depth = 0
    }

    data class Year(val year: StudyYear) : PlanSheetPage {
        override val depth = 1
    }

    data object Edit : PlanSheetPage {
        override val depth = 1
    }

    // Leave-without-submitting confirmation, pushed over the wizard.
    data object ConfirmExit : PlanSheetPage {
        override val depth = 2
    }

    // Print/submit outcome, shown as a dedicated result page.
    data object Result : PlanSheetPage {
        override val depth = 1
    }
}

// What the user was doing when the confirm page interrupted them.
private enum class ExitIntent { CloseEditor, DismissSheet }

// Confirm page in the sheet's own language: body copy and the connected action pair —
// Continua leads wide on the brand fill, Esci trails smaller and tonal.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DiscardChangesPage(
    onContinue: () -> Unit,
    onExit: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Le scelte fatte finora non sono state inviate e andranno perse.",
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
                onClick = onExit,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Text("Esci", fontWeight = FontWeight.SemiBold)
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
private fun SheetBody(
    loaded: Boolean,
    plan: StudyPlan?,
    coursesByYear: Map<StudyYear, List<StudyPlanCourse>>,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onYearClick: (StudyYear) -> Unit,
    editAvailable: Boolean,
    onEdit: () -> Unit,
    printing: Boolean,
    onPrint: () -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    // Hold the loading state for a beat so quick fetches don't flash it.
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading
    // Stampa needs an actual plan to print; Modifica works even with no plan yet (first
    // compilation). The footer shows whichever actions apply, none while loading/failed.
    val showFooter = settled && failure == null && (editAvailable || plan != null)

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
            failure != null && plan == null -> SheetError(cause = failure.cause, onRetry = onRetry)

            !settled -> SheetLoadingIndicator(label = "Caricamento piano…")

            plan == null -> SheetMessage(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                title = "Nessun piano di studi",
                body = "Non risulta alcun piano di studi per la tua carriera.",
            )

            else -> YearList(
                coursesByYear = coursesByYear,
                onYearClick = onYearClick,
                bottomPadding = if (showFooter) 12.dp else 24.dp,
            )
        }

        if (showFooter) {
            PlanActionFooter(
                editAvailable = editAvailable,
                printAvailable = plan != null,
                printing = printing,
                onEdit = onEdit,
                onPrint = onPrint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            )
        }
    }
}

// Connected expressive footer pair, same scheme as the edifici action row: the brand
// "Modifica percorso" leads wide, the tonal "Stampa" trails smaller. Either renders alone
// (full width, regular shape) when the other doesn't apply.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlanActionFooter(
    editAvailable: Boolean,
    printAvailable: Boolean,
    printing: Boolean,
    onEdit: () -> Unit,
    onPrint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary
    val paired = editAvailable && printAvailable

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (printAvailable) {
            FilledTonalButton(
                onClick = onPrint,
                enabled = !printing,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = if (paired) ButtonGroupDefaults.connectedLeadingButtonShape else ButtonDefaults.shape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                if (printing) {
                    LoadingIndicator(
                        modifier = Modifier.size(20.dp),
                        color = scheme.onSurface,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Print,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("Stampa", fontWeight = FontWeight.SemiBold)
            }
        }

        if (editAvailable) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = if (paired) ButtonGroupDefaults.connectedTrailingButtonShape else ButtonDefaults.shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandBg,
                    contentColor = brandFg,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Modifica percorso", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// "Percorso comune · piano standard": the percorso facet plus the plan flavor, the pair the
// removed in-page hero used to lead with.
private fun headerSubtitle(plan: StudyPlan?, path: StudyPath?): String {
    val percorso = path?.percorso?.label?.takeIf { it.isNotBlank() }?.displayCase()
        ?: if (path?.choiceAvailable == true) "Percorso" else "Percorso unico"
    val piano = plan?.type?.label() ?: return percorso
    return "$percorso · $piano"
}

// Esse3 percorso descriptions arrive ALL CAPS ("PERCORSO COMUNE"); sentence-case them for
// display but leave mixed-case ones (already human-authored) untouched.
private fun String.displayCase(): String =
    if (any { it.isLowerCase() }) this
    else lowercase().replaceFirstChar { it.titlecase() }

private fun StudyPlanType.label(): String = when (this) {
    StudyPlanType.Standard -> "piano standard"
    StudyPlanType.Individual -> "piano individuale"
    StudyPlanType.Unknown -> "piano di studi"
}

private fun yearSummary(courses: List<StudyPlanCourse>): String {
    val credits = courses.sumOf { it.credits.toDouble() }.toInt()
    return "${courses.size} attività · $credits CFU"
}

// Headerless year directory: one chevron row per academic year, rendered as a connected
// segmented group. Tapping pushes the year's course page in the sheet pager.
@Composable
private fun YearList(
    coursesByYear: Map<StudyYear, List<StudyPlanCourse>>,
    onYearClick: (StudyYear) -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val years = coursesByYear.entries.toList()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(years, key = { _, (year, _) -> year.value }) { index, (year, courses) ->
            YearRow(
                year = year,
                subtitle = yearSummary(courses),
                isFirst = index == 0,
                isLast = index == years.lastIndex,
                onClick = { onYearClick(year) },
            )
        }
    }
}

// Same anatomy as the edifici rows: 28dp corners cap the group's ends, 6dp where rows
// touch, a primary-container circle badge carrying the year number, emphasized title and
// a chevron leading into the year's course page.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun YearRow(
    year: StudyYear,
    subtitle: String,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = if (isFirst) 28.dp else 6.dp,
            topEnd = if (isFirst) 28.dp else 6.dp,
            bottomStart = if (isLast) 28.dp else 6.dp,
            bottomEnd = if (isLast) 28.dp else 6.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = scheme.primaryContainer) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (year == StudyYear.Unknown) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            text = "${year.value}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onPrimaryContainer,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = year.label(),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
            )
        }
    }
}

// Headerless course list for one year: the sheet's morphing header carries the year label.
@Composable
private fun YearCoursesPage(
    courses: List<StudyPlanCourse>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(courses, key = { _, course -> course.id }) { index, course ->
            CourseTile(
                course = course,
                isFirst = index == 0,
                isLast = index == courses.lastIndex,
            )
        }
    }
}

@Composable
private fun CourseTile(course: StudyPlanCourse, isFirst: Boolean, isLast: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
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
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${course.credits.toInt()}",
                        color = scheme.onPrimaryContainer,
                        fontSize = 15.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = "CFU",
                        color = scheme.onPrimaryContainer.copy(alpha = 0.75f),
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Medium,
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
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                course.code?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetError(cause: Throwable, onRetry: () -> Unit) {
    SheetMessage(
        icon = Icons.Outlined.CloudOff,
        title = "Caricamento non riuscito",
        body = cause.friendlyMessage(),
        action = { RetryButton(onClick = onRetry) },
    )
}

private fun StudyYear.label(): String =
    if (this == StudyYear.Unknown) "ALTRE ATTIVITÀ" else "$value° ANNO"
