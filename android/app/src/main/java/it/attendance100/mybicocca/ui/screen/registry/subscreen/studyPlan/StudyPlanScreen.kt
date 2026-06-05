package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.studyplan.StudyPath
import it.attendance100.mybicocca.domain.model.studyplan.StudyPathOption
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlan
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanCourse
import it.attendance100.mybicocca.domain.model.studyplan.StudyPlanType
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.state.StudyPlanEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StudyPlanScreen(
    onOpenEdit: (studentId: Long, choiceRegulationId: Long, schemaId: Long, planId: Long) -> Unit = { _, _, _, _ -> },
    viewModel: StudyPlanViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val planData by viewModel.plan.collectAsStateWithLifecycle()
    val pathData by viewModel.studyPath.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val editable by viewModel.editable.collectAsStateWithLifecycle()
    val actionInProgress by viewModel.actionInProgress.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is StudyPlanEvent.OpenPdf -> openPdfDocument(context, event.bytes, event.fileName)
                is StudyPlanEvent.ShowMessage -> scope.launch { snackbar.showInfo(event.message) }
            }
        }
    }

    val pullState = rememberPullToRefreshState()
    var pullIndicatorVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = pullIndicatorVisible,
            onRefresh = {
                pullIndicatorVisible = true
                viewModel.pullToRefresh()
                scope.launch {
                    delay(PULL_INDICATOR_DISMISS_DELAY_MS)
                    pullIndicatorVisible = false
                }
            },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val snapshot = planData) {
                Loadable.NotYetLoaded -> when (val status = syncStatus) {
                    is SyncStatus.Failed -> RefreshableEmpty {
                        ErrorEmptyState(cause = status.cause, onRetry = viewModel::refresh)
                    }

                    else -> RefreshableEmpty {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LoadingIndicator(modifier = Modifier.size(72.dp))
                        }
                    }
                }

                is Loadable.Loaded -> {
                    val plan = snapshot.value
                    val failure = syncStatus as? SyncStatus.Failed
                    when {
                        failure != null && plan == null -> RefreshableEmpty {
                            ErrorEmptyState(cause = failure.cause, onRetry = viewModel::refresh)
                        }

                        plan == null -> RefreshableEmpty {
                            EmptyState(
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                title = "Nessun piano di studi",
                                body = "Non risulta alcun piano di studi per la tua carriera.",
                            )
                        }

                        else -> PlanContent(
                            plan = plan,
                            studyPath = pathData.valueOrNull(),
                            printing = actionInProgress,
                            onPrint = viewModel::printPlan,
                            onChoosePath = viewModel::chooseStudyPath,
                        )
                    }
                }
            }
        }

        // Single floating action: editing the plan. Printing lives on the plan card.
        val plan = planData.valueOrNull()
        if (plan != null) {
            val regulationId = plan.choiceRegulationId
            val schemaId = plan.schemaId
            if (editable && regulationId != null && schemaId != null) {
                ExtendedFloatingActionButton(
                    onClick = { onOpenEdit(plan.studentId, regulationId, schemaId, plan.planId) },
                    icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    text = { Text("Modifica piano") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun PlanContent(
    plan: StudyPlan,
    studyPath: StudyPath?,
    printing: Boolean,
    onPrint: () -> Unit,
    onChoosePath: (schemaId: Long) -> Unit,
) {
    // Years ascending, with the generic "no specific year" bucket at the end.
    val coursesByYear = remember(plan) {
        plan.courses.groupBy { it.year }
            .toSortedMap(compareBy { if (it == StudyYear.Unknown) Int.MAX_VALUE else it.value })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "plan_hero") {
            PlanHeroCard(plan = plan, printing = printing, onPrint = onPrint)
        }

        if (studyPath != null && studyPath.hasAnyFacet) {
            item(key = "study_path") {
                StudyPathSection(path = studyPath, onChoose = onChoosePath)
            }
        }

        coursesByYear.forEach { (year, courses) ->
            item(key = "year_${year.value}") {
                YearSection(title = year.label(), courses = courses)
            }
        }
    }
}

// "Percorso" section: the current path facets as a connected tile group, plus the
// selectable alternatives when a choice window is open and more than one option exists.
@Composable
private fun StudyPathSection(
    path: StudyPath,
    onChoose: (schemaId: Long) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    val facets = remember(path) {
        buildList {
            path.percorso?.let { add("Percorso" to it.label) }
            path.orientamento?.let { add("Orientamento" to it.label) }
            path.profilo?.let { add("Profilo" to it.label) }
            path.partTime?.let { add("Impegno" to it.label) }
        }.filter { it.second.isNotBlank() }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        YearHeaderTile(
            title = "Percorso",
            subtitle = if (path.choiceAvailable) {
                "Scelta disponibile · ${path.options.size} opzioni"
            } else {
                "Percorso unico · nessuna scelta disponibile"
            },
        )

        facets.forEachIndexed { index, (label, value) ->
            FacetTile(
                label = label,
                value = value,
                isLast = !path.choiceAvailable && index == facets.lastIndex,
            )
        }

        if (path.choiceAvailable) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Scegli il tuo percorso",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                path.options.forEach { option ->
                    PathOptionCard(option = option, onClick = { onChoose(option.schemaId) })
                }
            }
        }
    }
}

@Composable
private fun FacetTile(label: String, value: String, isLast: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(2f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PathOptionCard(option: StudyPathOption, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val container = if (option.isCurrent) scheme.secondaryContainer else scheme.surfaceContainer
    val onContainer = if (option.isCurrent) scheme.onSecondaryContainer else scheme.onSurface

    Surface(
        onClick = onClick,
        enabled = !option.isCurrent,
        modifier = Modifier.fillMaxWidth(),
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(20.dp),
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
                    color = onContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val detail = listOfNotNull(
                    option.orientamento?.label?.takeIf { it.isNotBlank() },
                    option.profilo?.label?.takeIf { it.isNotBlank() },
                    option.partTime?.label?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainer.copy(alpha = 0.75f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            if (option.isCurrent) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Attuale",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    tint = onContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// Hero copied from the reference design: a deep-wine rounded card with a huge circle
// blob bleeding off the top-right, an upright flower that IS the print button, a vibrant
// status pill at the top and the title/stats/meta block anchored at the bottom.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlanHeroCard(
    plan: StudyPlan,
    printing: Boolean,
    onPrint: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    // 8-petal flower, matching the reference's clip-path.
    val glyph = remember { MaterialShapes.Flower }.toShape()
    val totalCredits = remember(plan) { plan.courses.sumOf { it.credits.toDouble() }.toInt() }
    // The reference card sits between surface and primaryContainer — a deeper wine in
    // dark, a softer rose in light.
    val container = scheme.primaryContainer.copy(alpha = 0.55f).compositeOver(scheme.surface)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = container,
        contentColor = scheme.onPrimaryContainer,
        shape = RoundedCornerShape(30.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(210.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-44).dp)
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(scheme.onPrimaryContainer.copy(alpha = 0.08f)),
            )
            // The flower itself is the print action: tap to fetch the official PDF.
            Surface(
                onClick = onPrint,
                enabled = !printing,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 36.dp, end = 24.dp)
                    .size(90.dp)
                    .graphicsLayer { rotationZ = 14f },
                shape = glyph,
                color = scheme.surface,
                contentColor = scheme.primary,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (printing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = scheme.primary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Print,
                            contentDescription = "Stampa piano",
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                plan.statusDescription?.let {
                    StatusPill(text = it, modifier = Modifier.align(Alignment.TopStart))
                }

                Column(Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = plan.type.label(),
                        fontSize = 32.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp,
                        color = scheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HeroStat(value = "${plan.courses.size}", unit = "attività")
                        MetaDot(color = scheme.onPrimaryContainer)
                        HeroStat(value = "$totalCredits", unit = "CFU")
                    }
                    plan.lastUpdated?.let {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            HeroFootText("ULTIMA MODIFICA")
                            MetaDot(color = scheme.onPrimaryContainer.copy(alpha = 0.65f))
                            HeroFootText(it.format(PlanDateFormat).uppercase())
                        }
                    }
                }
            }
        }
    }
}

// Vibrant mint status pill with a check, fixed across themes for pop (like the
// reference's "Approvato" badge).
@Composable
private fun StatusPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = StatusPillGreen,
        contentColor = OnStatusPillGreen,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = text.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// "24 attività" — the number heavy, the unit lighter, per the reference meta line.
@Composable
private fun HeroStat(value: String, unit: String) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(value) }
            append(" $unit")
        },
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = scheme.onPrimaryContainer,
        maxLines = 1,
    )
}

@Composable
private fun HeroFootText(text: String) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.7.sp,
        color = scheme.onPrimaryContainer.copy(alpha = 0.65f),
        maxLines = 1,
    )
}

@Composable
private fun MetaDot(color: Color) {
    Box(
        modifier = Modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.55f)),
    )
}

// One academic year rendered like the registry directory sections: a header tile and
// connected sub-tiles with 2.dp seams.
@Composable
private fun YearSection(
    title: String,
    courses: List<StudyPlanCourse>,
) {
    val yearCredits = remember(courses) { courses.sumOf { it.credits.toDouble() }.toInt() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        YearHeaderTile(
            title = title,
            subtitle = "${courses.size} attività · $yearCredits CFU",
        )
        courses.forEachIndexed { index, course ->
            CourseTile(course = course, isLast = index == courses.lastIndex)
        }
    }
}

@Composable
private fun YearHeaderTile(
    title: String,
    subtitle: String,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 12.dp
            )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CourseTile(course: StudyPlanCourse, isLast: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        modifier = Modifier.fillMaxWidth(),
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

// Wraps full-screen empty/loading/error content in a scrollable container so the
// pull-to-refresh gesture still works when there's nothing else to scroll.
@Composable
private fun RefreshableEmpty(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize()) { content() }
        }
    }
}

@Composable
private fun ErrorEmptyState(cause: Throwable, onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Outlined.CloudOff,
        title = "Caricamento non riuscito",
        body = cause.friendlyMessage(),
        action = { FilledTonalButton(onClick = onRetry) { Text("Riprova") } },
    )
}

private fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."

    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto. Riprova."
}

private fun StudyPlanType.label(): String = when (this) {
    StudyPlanType.Standard -> "Piano standard"
    StudyPlanType.Individual -> "Piano individuale"
    StudyPlanType.Unknown -> "Piano di studi"
}

private fun StudyYear.label(): String =
    if (this == StudyYear.Unknown) "Altre attività" else "Anno $value"

private val PlanDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

// Reference status-badge colors, fixed across themes (vibrant mint on the wine card).
private val StatusPillGreen = Color(0xFF9DF0BC)
private val OnStatusPillGreen = Color(0xFF00210F)

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
