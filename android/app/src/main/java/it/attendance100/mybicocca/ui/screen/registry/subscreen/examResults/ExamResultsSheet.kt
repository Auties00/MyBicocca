package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Grading
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.model.exam.requiresStudentDecision
import it.attendance100.mybicocca.ui.component.SegmentedSwitch
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.card.DetailFactCard
import it.attendance100.mybicocca.ui.component.exam.ExamGradeBadge
import it.attendance100.mybicocca.ui.component.exam.shortLabel
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.groupByFilter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Esiti as a modal sheet, same modal language as edifici and prenotazioni: a pinned
// morphing header over a three-level body pager (esiti feed -> esito detail -> reject
// confirmation). The feed splits into the outcomes still waiting for the student's
// accept/reject decision and the already-published register beneath them; the grade
// leads everywhere, in an expressive polygon whose shape encodes the outcome.
// Esiti as a single sheet entry: the container is owned by BottomSheetSceneStrategy, but this
// keeps its own internal page state machine (feed -> detail -> reject confirm / result) and its
// own morphing header, since the confirm and result pages are ephemeral, not real destinations.
@Composable
fun ExamResultsPage(
    viewModel: ExamResultsViewModel,
) {
    val resultsData by viewModel.results.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    // The VM outlives the sheet (it is shell-scoped, not route-scoped). The first open is
    // loaded by the VM's own career collector; a re-open shows the cached snapshot instantly
    // while this kicks a background refresh.
    LaunchedEffect(viewModel) {
        if (viewModel.results.value is Loadable.Loaded) viewModel.refresh()
    }

    val loaded = resultsData is Loadable.Loaded
    val results = resultsData.valueOrNull().orEmpty()
    val today = remember { LocalDate.now() }

    val grouped = remember(results, today) { results.groupByFilter(today) }

    // Which section the root pager is showing — tracked only so the header subtitle follows
    // the visible tab. The pager owns the real paging state and drives this via
    // onSectionChange (including its initial landing tab), so it must NOT be re-keyed on the
    // data or a refresh would reset it out from under the pager.
    var section by remember { mutableStateOf(ExamResultFilter.Pending) }

    // The detail page's esito is re-derived from the live list by identity: a refresh
    // updates it in place, and the optimistic removal after an accept/reject drops the
    // page back to the feed instead of rendering a stale snapshot.
    var detailId by remember { mutableStateOf<String?>(null) }
    // True while the rifiuta confirmation page is pushed over the detail.
    var confirmingReject by remember { mutableStateOf(false) }
    // Which half of the action pair carries the in-flight spinner (true = accetta).
    var acceptInFlight by remember { mutableStateOf(false) }
    // The outcome of an accept/reject, shown as a dedicated result page instead of a snackbar.
    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    val detailResult = detailId?.let { id -> results.firstOrNull { it.identity() == id } }

    val page = when {
        outcome != null -> EsitiSheetPage.Result
        detailResult == null -> EsitiSheetPage.Root
        confirmingReject -> EsitiSheetPage.ConfirmReject
        else -> EsitiSheetPage.Detail
    }

    run {
        LaunchedEffect(viewModel) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    ExamResultEvent.AcceptSucceeded -> {
                        confirmingReject = false
                        detailId = null
                        outcome = SheetOutcome.Success("Esito accettato")
                    }
                    ExamResultEvent.RejectSucceeded -> {
                        confirmingReject = false
                        detailId = null
                        outcome = SheetOutcome.Success("Esito rifiutato")
                    }
                    is ExamResultEvent.Failed ->
                        outcome = SheetOutcome.Error("Operazione non riuscita", event.cause)
                }
            }
        }

        // System back walks the pager up one level before dismissing the sheet.
        BackHandler(enabled = page != EsitiSheetPage.Root) {
            when {
                outcome != null -> outcome = null
                confirmingReject -> confirmingReject = false
                else -> detailId = null
            }
        }

        Column {
            SheetPagerHeader(
                depth = page.depth,
                title = when (page) {
                    EsitiSheetPage.Root -> "Esiti"
                    EsitiSheetPage.Detail -> detailResult?.displayTitle() ?: ""
                    EsitiSheetPage.ConfirmReject -> "Rifiutare l'esito?"
                    EsitiSheetPage.Result -> ""
                },
                subtitle = when (page) {
                    EsitiSheetPage.Result -> null
                    EsitiSheetPage.Root -> if (loaded) sectionSummary(section, grouped) else null
                    EsitiSheetPage.Detail -> detailResult?.let { result ->
                        result.acknowledgmentDeadline
                            ?.takeIf { result.requiresStudentDecision(today) }
                            ?.let { "Decidi entro il ${it.format(ShortDateFormat)}" }
                            ?: result.examDateTime?.let { "Sostenuto il ${it.toLocalDate().format(ShortDateFormat)}" }
                            ?: result.grade.spelledOut()
                    }
                    EsitiSheetPage.ConfirmReject -> detailResult?.displayTitle()
                },
                onBack = when (page) {
                    EsitiSheetPage.Root -> null
                    EsitiSheetPage.Detail -> ({ detailId = null })
                    EsitiSheetPage.ConfirmReject -> ({ confirmingReject = false })
                    EsitiSheetPage.Result -> null
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
                        EsitiSheetPage.Root -> "root"
                        EsitiSheetPage.Detail -> "detail"
                        EsitiSheetPage.ConfirmReject -> "confirm_reject"
                        EsitiSheetPage.Result -> "result"
                    }
                },
                label = "esiti_sheet_pages",
            ) { target ->
                when (target) {
                    EsitiSheetPage.Root -> SheetBody(
                        loaded = loaded,
                        grouped = grouped,
                        today = today,
                        syncStatus = syncStatus,
                        onRetry = viewModel::refresh,
                        onSectionChange = { section = it },
                        onOpenDetail = { detailId = it.identity() },
                    )

                    EsitiSheetPage.Detail -> detailResult?.let { result ->
                        EsitoDetailPage(
                            result = result,
                            decisionPending = result.requiresStudentDecision(today),
                            inProgress = (actionState as? ExamResultActionState.InProgress)
                                ?.applicationListId == result.applicationListId,
                            acceptInFlight = acceptInFlight,
                            actionEnabled = actionState is ExamResultActionState.Idle,
                            onAccept = {
                                acceptInFlight = true
                                viewModel.accept(result)
                            },
                            onRequestReject = { confirmingReject = true },
                        )
                    }

                    EsitiSheetPage.ConfirmReject -> detailResult?.let { result ->
                        RejectConfirmPage(
                            result = result,
                            onKeep = { confirmingReject = false },
                            onConfirm = {
                                // Back to the detail, whose Rifiuta button carries the
                                // in-flight spinner; success pops the pager to the feed.
                                confirmingReject = false
                                acceptInFlight = false
                                viewModel.reject(result)
                            },
                        )
                    }

                    EsitiSheetPage.Result -> outcome?.let { current ->
                        SheetResultPage(outcome = current, onDismiss = { outcome = null })
                    }
                }
            }
        }
    }
}

private sealed interface EsitiSheetPage {
    val depth: Int

    data object Root : EsitiSheetPage {
        override val depth = 0
    }

    data object Detail : EsitiSheetPage {
        override val depth = 1
    }

    // Rifiuta-esito confirmation, pushed over the detail.
    data object ConfirmReject : EsitiSheetPage {
        override val depth = 2
    }

    // Accept/reject outcome, shown as a dedicated result page.
    data object Result : EsitiSheetPage {
        override val depth = 1
    }
}

// applicationListId is unique per booking row; outcomes without one (never actionable)
// fall back to the call key.
private fun ExamResult.identity(): String =
    applicationListId?.toString()
        ?: "${key.courseOfStudyId}-${key.activityId}-${key.callId}"

private fun ExamResult.displayTitle(): String = activityDescription ?: "Esame"

// The header subtitle for the visible section: how full it is.
private fun sectionSummary(
    section: ExamResultFilter,
    grouped: Map<ExamResultFilter, List<ExamResult>>,
): String? {
    val count = grouped[section].orEmpty().size
    return when (section) {
        ExamResultFilter.Pending -> when (count) {
            0 -> "Nessun esito da decidere"
            1 -> "1 esito da decidere"
            else -> "$count esiti da decidere"
        }
        ExamResultFilter.Archived -> when (count) {
            0 -> "Nessun esito archiviato"
            1 -> "1 esito archiviato"
            else -> "$count esiti archiviati"
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetBody(
    loaded: Boolean,
    grouped: Map<ExamResultFilter, List<ExamResult>>,
    today: LocalDate,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onSectionChange: (ExamResultFilter) -> Unit,
    onOpenDetail: (ExamResult) -> Unit,
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
            failure != null && !loaded -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Caricamento non riuscito",
                body = failure.cause.friendlyMessage(),
                action = { RetryButton(onClick = onRetry) },
            )

            !settled -> SheetLoadingIndicator(label = "Caricamento esiti…")

            grouped.values.all { it.isEmpty() } -> SheetMessage(
                icon = Icons.Outlined.Grading,
                title = "Nessun esito pubblicato",
                body = "Quando un docente pubblica l'esito di un appello lo troverai qui.",
            )

            else -> EsitiPager(
                grouped = grouped,
                today = today,
                onSectionChange = onSectionChange,
                onOpenDetail = onOpenDetail,
            )
        }
    }
}

@Composable
private fun EsitiPager(
    grouped: Map<ExamResultFilter, List<ExamResult>>,
    today: LocalDate,
    onSectionChange: (ExamResultFilter) -> Unit,
    onOpenDetail: (ExamResult) -> Unit,
) {
    val filters = ExamResultFilter.entries
    // Land on In sospeso when it has anything, else Archiviati (which is index 1).
    val initialPage = remember(grouped) {
        filters.indexOfFirst { grouped[it].orEmpty().isNotEmpty() }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { filters.size }
    val scope = rememberCoroutineScope()

    // Report the visible tab up so the header subtitle follows swipes and taps.
    LaunchedEffect(pagerState.targetPage) { onSectionChange(filters[pagerState.targetPage]) }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            // Fixed height so the modal stays the same size across both tabs and the swipe
            // doesn't snap at the end when the two pages' content heights differ.
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp),
            verticalAlignment = Alignment.Top,
        ) { page ->
            val filter = filters[page]
            val items = grouped[filter].orEmpty()
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    EmptyState(
                        icon = filter.emptyIcon(),
                        title = filter.emptyTitle(),
                        body = filter.emptyBody(),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(items, key = { _, result -> "${filter.name}_${result.identity()}" }) { _, result ->
                        if (filter == ExamResultFilter.Pending) {
                            PendingResultRow(
                                result = result,
                                today = today,
                                onClick = { onOpenDetail(result) },
                            )
                        } else {
                            ArchivedResultRow(
                                result = result,
                                onClick = { onOpenDetail(result) },
                            )
                        }
                    }
                }
            }
        }

        // targetPage (not currentPage) so the switch tracks the slide as soon as a swipe commits.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(100)),
        ) {
            SegmentedSwitch(
                options = filters,
                selected = filters[pagerState.targetPage],
                onSelected = { filter ->
                    scope.launch { pagerState.animateScrollToPage(filters.indexOf(filter)) }
                },
                label = { it.label },
                borderColor = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

private fun ExamResultFilter.emptyIcon(): ImageVector = when (this) {
    ExamResultFilter.Pending -> Icons.Outlined.TaskAlt
    ExamResultFilter.Archived -> Icons.Outlined.Grading
}

private fun ExamResultFilter.emptyTitle(): String = when (this) {
    ExamResultFilter.Pending -> "Niente da decidere"
    ExamResultFilter.Archived -> "Nessun esito archiviato"
}

private fun ExamResultFilter.emptyBody(): String = when (this) {
    ExamResultFilter.Pending -> "Non hai esiti in attesa di una tua decisione."
    ExamResultFilter.Archived -> "Gli esiti accettati, rifiutati o verbalizzati compaiono qui."
}

// An outcome still waiting for the student's choice, as a standalone card: the grade polygon
// leads and the subtitle says what's asked, folding in the countdown when a deadline exists.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PendingResultRow(
    result: ExamResult,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ExamGradeBadge(grade = result.grade, size = 52.dp, style = MaterialTheme.typography.titleMediumEmphasized)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = result.displayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                result.acknowledgmentDeadline?.let { deadline ->
                    val daysLeft = ChronoUnit.DAYS.between(today, deadline)
                    Text(
                        // "Decidi entro" stays in the normal supporting color; only the deadline
                        // itself (oggi / domani / N giorni) carries the urgent brand red.
                        text = buildAnnotatedString {
                            append("Decidi entro ")
                            withStyle(SpanStyle(color = scheme.primary)) {
                                append(deadlineLabel(daysLeft))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

private fun deadlineLabel(daysLeft: Long): String = when {
    daysLeft <= 0L -> "oggi"
    daysLeft == 1L -> "domani"
    else -> "$daysLeft giorni"
}

// Register row for an already-settled outcome: same card and grade polygon as the pending
// ones, with a plain-language subtitle of what happened to it.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ArchivedResultRow(
    result: ExamResult,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ExamGradeBadge(grade = result.grade, size = 52.dp, style = MaterialTheme.typography.titleMediumEmphasized)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = result.displayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                result.archivedSubtitle()?.let { supporting ->
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// What became of a published outcome, in a readable line: the student's choice when they
// made one, otherwise when the exam was taken.
private fun ExamResult.archivedSubtitle(): String? =
    acknowledgment.choiceLabel()
        ?: examDateTime?.let { "Sostenuto il ${it.toLocalDate().format(MediumDateFormat)}" }

// Only states the student actively produced are worth a word; everything else is just
// a published grade.
private fun AcknowledgmentStatus.choiceLabel(): String? = when (this) {
    AcknowledgmentStatus.Accepted -> "Accettato"
    AcknowledgmentStatus.Rejected -> "Rifiutato"
    else -> null
}

// The moment of truth: the grade fills the stage in its outcome shape — the "/30" tucked
// inside it as a smaller symbol — with the facts wrapped in cards beneath and, when a
// decision is open, a warning card and the connected reject/accept pair pinned at the bottom.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EsitoDetailPage(
    result: ExamResult,
    decisionPending: Boolean,
    inProgress: Boolean,
    acceptInFlight: Boolean,
    actionEnabled: Boolean,
    onAccept: () -> Unit,
    onRequestReject: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 640.dp)
            .padding(bottom = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ExamGradeBadge(
                    grade = result.grade,
                    size = 112.dp,
                    style = if (result.grade.shortLabel().length <= 3) {
                        MaterialTheme.typography.displaySmallEmphasized
                    } else {
                        MaterialTheme.typography.headlineMediumEmphasized
                    },
                    showDenominator = true,
                )
            }

            Spacer(Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                result.examDateTime?.let {
                    DetailFactCard(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "SOSTENUTO IL",
                        value = it.toLocalDate().format(FullDateFormat)
                            .replaceFirstChar { c -> c.titlecase(Locale.ITALIAN) },
                    )
                }
                result.publishedNote?.takeIf { it.isNotBlank() }?.let {
                    DetailFactCard(
                        icon = Icons.AutoMirrored.Outlined.Notes,
                        label = "NOTA DEL DOCENTE",
                        value = it,
                    )
                }
                result.acknowledgment.choiceLabel()?.let {
                    DetailFactCard(
                        icon = Icons.Outlined.TaskAlt,
                        label = "LA TUA SCELTA",
                        value = it,
                    )
                }
            }

            Spacer(Modifier.height(if (decisionPending) 20.dp else 8.dp))
        }

        if (decisionPending) {
            DecisionActionRow(
                inProgress = inProgress,
                acceptInFlight = acceptInFlight,
                enabled = actionEnabled,
                onAccept = onAccept,
                onRequestReject = onRequestReject,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

// Connected pair: the secondary-tinted half opens the reject confirmation, the wider brand
// half accepts — same arrangement as the booked-exam and appointment sheets, mirrored.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DecisionActionRow(
    inProgress: Boolean,
    acceptInFlight: Boolean,
    enabled: Boolean,
    onAccept: () -> Unit,
    onRequestReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            onClick = onRequestReject,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = scheme.surfaceContainerHighest,
                contentColor = scheme.onSurface,
                disabledContainerColor = scheme.surfaceContainerHighest.copy(alpha = 0.55f),
                disabledContentColor = scheme.onSurface.copy(alpha = 0.55f),
            ),
        ) {
            if (inProgress && !acceptInFlight) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = scheme.onSurface,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Rifiuta", fontWeight = FontWeight.SemiBold)
            }
        }
        Button(
            onClick = onAccept,
            enabled = enabled,
            modifier = Modifier
                .weight(1.4f)
                .height(56.dp),
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = brandBg,
                contentColor = brandFg,
                disabledContainerColor = brandBg.copy(alpha = 0.55f),
                disabledContentColor = brandFg.copy(alpha = 0.55f),
            ),
        ) {
            if (inProgress && acceptInFlight) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = brandFg,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Accetta", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Confirm page in the sheet's own language, replacing a stacked dialog: body copy and the
// connected action pair — the safe Annulla trails wide on the brand fill (the recommended way
// out), while the destructive Conferma leads smaller on the neutral tonal.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RejectConfirmPage(
    result: ExamResult,
    onKeep: () -> Unit,
    onConfirm: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Stai per rifiutare ${result.grade.spelledOut()} in ${result.displayTitle()}. " +
                "Il voto verrà annullato e per superare l'esame dovrai iscriverti a un nuovo appello. " +
                "La decisione non può essere annullata.",
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
                Text("Conferma", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onKeep,
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandBg,
                    contentColor = brandFg,
                ),
            ) {
                Text("Annulla", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// The grade in words, like on the verbale.
private fun ExamGrade.spelledOut(): String = when (this) {
    is ExamGrade.Numeric -> when {
        value >= 31 -> "trenta e lode"
        else -> "${GradeWords[value] ?: value.toString()} su trenta"
    }
    ExamGrade.Passed -> "idoneo"
    ExamGrade.NotPassed -> "non superato"
    ExamGrade.Withdrew -> "ritirato"
    ExamGrade.Absent -> "assente"
    ExamGrade.Unknown -> "esito non disponibile"
}

private val GradeWords = mapOf(
    18 to "diciotto", 19 to "diciannove", 20 to "venti", 21 to "ventuno",
    22 to "ventidue", 23 to "ventitré", 24 to "ventiquattro", 25 to "venticinque",
    26 to "ventisei", 27 to "ventisette", 28 to "ventotto", 29 to "ventinove",
    30 to "trenta",
)

private val ShortDateFormat = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)
private val MediumDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)
private val FullDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
