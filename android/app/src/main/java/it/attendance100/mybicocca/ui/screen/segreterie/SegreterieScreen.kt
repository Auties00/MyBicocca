package it.attendance100.mybicocca.ui.screen.segreterie

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.R.drawable
import it.attendance100.mybicocca.R.string
import it.attendance100.mybicocca.ui.component.AutoScrollingFilterRow
import it.attendance100.mybicocca.ui.component.DualActionBottomBar
import it.attendance100.mybicocca.ui.component.NetworkStatusBar
import it.attendance100.mybicocca.ui.component.card.SimpleCard
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonSegreterieContent
import it.attendance100.mybicocca.util.rememberHapticManager
import it.attendance100.mybicocca.util.rememberPreferencesManager
import kotlinx.coroutines.delay
import kotlin.random.Random

sealed interface DashboardItem {
    val id: String
    val title: String
}

enum class DashboardTileStatus {
    Normal,
    Warning,
    Important,
}

enum class DashboardCategory {
    Tutti,
    Esami,
    Didattica,
    Amministrazione,
    Agenda,
    Stage,
}

@Composable
fun DashboardCategory.label(): String {
    return when (this) {
        DashboardCategory.Tutti -> stringResource(string.segreterie_filter_all)
        DashboardCategory.Esami -> stringResource(string.segreterie_filter_exams)
        DashboardCategory.Didattica -> stringResource(string.segreterie_filter_didattica)
        DashboardCategory.Amministrazione -> stringResource(string.segreterie_filter_financial)
        DashboardCategory.Agenda -> stringResource(string.segreterie_filter_agenda)
        DashboardCategory.Stage -> stringResource(string.segreterie_filter_stage)
    }
}

data class DashboardHeader(
    override val id: String,
    override val title: String,
) : DashboardItem

data class DashboardTile(
    override val id: String,
    override val title: String,
    val subtitle: String? = null,
    val status: DashboardTileStatus = DashboardTileStatus.Normal,
    val icon: ImageVector,
    val action: () -> Unit,
    val heightDp: Dp = if (subtitle != null) 95.dp else 70.dp,
    val category: DashboardCategory = DashboardCategory.Tutti,
) : DashboardItem

private object SegreterieAnimationState {
    var shown = false
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SegreterieScreen(
    onNavigateToBooking: () -> Unit = {},
    onNavigateToBooked: () -> Unit = {},
    onNavigateToTaxes: () -> Unit = {},
    onNavigateToIsee: () -> Unit = {},
    onNavigateToSelfCertificates: () -> Unit = {},
    onNavigateToExamResults: () -> Unit = {},
    onNavigateToPianoCarriera: () -> Unit = {},
    onNavigateToQuestionnaires: () -> Unit = {},
    onNavigateToReservations: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToStage: () -> Unit = {},
    viewModel: SegreterieViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val haptic = rememberHapticManager()
    val unpaidTaxCount by viewModel.unpaidTaxCount.collectAsStateWithLifecycle()
    val bookedExamCount by viewModel.bookedExamCount.collectAsStateWithLifecycle()
    val availableExamCount by viewModel.availableExamCount.collectAsStateWithLifecycle()
    val examResultsCount by viewModel.examResultsCount.collectAsStateWithLifecycle()
    val studyPlanStatus by viewModel.studyPlanStatus.collectAsStateWithLifecycle()
    val studyPlanLastUpdated by viewModel.studyPlanLastUpdated.collectAsStateWithLifecycle()
    val questionnairePendingCount by viewModel.questionnairePendingCount.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val shouldAnimate = remember { !SegreterieAnimationState.shown }
    LaunchedEffect(Unit) {
        SegreterieAnimationState.shown = true
    }

    val gridItems: List<DashboardItem> = listOf(
        DashboardHeader(
            id = "exam_title",
            title = stringResource(string.segreterie_exams_booking),
        ),
        DashboardTile(
            id = "booked_booking",
            title = stringResource(string.segreterie_booking),
            subtitle = if (availableExamCount > 0) pluralStringResource(
                R.plurals.segreterie_booking_available,
                count = availableExamCount,
                availableExamCount
            ) else null,
            icon = Icons.Filled.Event,
            category = DashboardCategory.Esami,
            action = onNavigateToBooking,
        ),
        DashboardTile(
            id = "booked_booked",
            title = stringResource(string.segreterie_booked),
            subtitle = if (bookedExamCount > 0) pluralStringResource(
                R.plurals.segreterie_booked_number,
                count = bookedExamCount,
                bookedExamCount,
            ) else null,
            icon = Icons.Filled.EventAvailable,
            status = if (bookedExamCount > 0) DashboardTileStatus.Important else DashboardTileStatus.Normal,
            category = DashboardCategory.Esami,
            action = onNavigateToBooked,
        ),
        DashboardHeader(
            id = "financial",
            title = stringResource(string.segreterie_services),
        ),
        DashboardTile(
            id = "taxes",
            title = stringResource(string.segreterie_taxes),
            subtitle = if (unpaidTaxCount == 0) {
                stringResource(string.segreterie_taxes_desc_zero)
            } else {
                pluralStringResource(
                    R.plurals.segreterie_taxes_desc,
                    count = unpaidTaxCount,
                    unpaidTaxCount,
                )
            },
            icon = Icons.Outlined.Euro,
            status = if (unpaidTaxCount > 0) DashboardTileStatus.Warning else DashboardTileStatus.Normal,
            category = DashboardCategory.Amministrazione,
            action = onNavigateToTaxes,
        ),
        DashboardTile(
            id = "isee",
            title = stringResource(string.segreterie_isee),
            icon = Icons.Outlined.AccountBalance,
            category = DashboardCategory.Amministrazione,
            action = onNavigateToIsee,
        ),
        DashboardTile(
            id = "scholarship",
            title = stringResource(string.segreterie_self_certificates),
            icon = Icons.Outlined.Description,
            category = DashboardCategory.Amministrazione,
            action = onNavigateToSelfCertificates,
        ),
        DashboardTile(
            id = "exam_results",
            title = "Bacheca Esami",
            subtitle = if (examResultsCount > 0) "$examResultsCount voti pubblicati" else null,
            icon = ImageVector.vectorResource(drawable.clarify),
            category = DashboardCategory.Esami,
            action = onNavigateToExamResults,
        ),
        DashboardTile(
            id = "plan",
            title = "Piano Studi",
            subtitle = studyPlanStatus,
            icon = Icons.Filled.Book,
            category = DashboardCategory.Didattica,
            action = onNavigateToPianoCarriera,
        ),
        DashboardTile(
            id = "questionnaires",
            title = "Questionari",
            subtitle = if (questionnairePendingCount > 0) {
                "$questionnairePendingCount da compilare"
            } else {
                "Tutti compilati"
            },
            icon = ImageVector.vectorResource(drawable.stylus_note),
            category = DashboardCategory.Didattica,
            status = if (questionnairePendingCount > 0) DashboardTileStatus.Warning
            else DashboardTileStatus.Normal,
            action = onNavigateToQuestionnaires,
        ),
        DashboardTile(
            id = "reservations",
            title = "Prenotazioni",
            subtitle = null,
            icon = Icons.Filled.AutoStories,
            category = DashboardCategory.Agenda,
            action = onNavigateToReservations,
        ),
        DashboardTile(
            id = "attendance",
            title = "Presenze",
            subtitle = null,
            icon = ImageVector.vectorResource(drawable.concierge),
            category = DashboardCategory.Agenda,
            action = onNavigateToAttendance,
        ),
        DashboardTile(
            id = "stage",
            title = "Stage",
            subtitle = null,
            icon = Icons.Filled.WorkHistory,
            category = DashboardCategory.Stage,
            action = onNavigateToStage,
        ),
    )

    var selectedCategory by remember { mutableStateOf(DashboardCategory.Tutti) }

    val allTiles = remember(gridItems) { gridItems.filterIsInstance<DashboardTile>() }

    val displayedCategories = remember(selectedCategory) {
        if (selectedCategory == DashboardCategory.Tutti) DashboardCategory.entries
        else listOf(selectedCategory)
    }

    val groupedTiles = remember(selectedCategory, allTiles) {
        val tiles = if (selectedCategory == DashboardCategory.Tutti) allTiles
        else allTiles.filter { it.category == selectedCategory }
        tiles.groupBy { it.category }
    }

    val delays = remember(groupedTiles) {
        val map = mutableMapOf<String, Int>()
        val heights = floatArrayOf(0f, 0f)

        displayedCategories.forEach { category ->
            groupedTiles[category]?.forEach { item ->
                val maxH = heights.maxOrNull() ?: 0f
                heights[0] = maxH
                heights[1] = maxH
            }
        }
        map
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        indicator = {},
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            isRefreshing -> {
                Column {
                    NetworkStatusBar(isOnline = isOnline, errorMessage = error, onDismissError = viewModel::clearError)
                    SkeletonSegreterieContent()
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        NetworkStatusBar(isOnline = isOnline, errorMessage = error, onDismissError = viewModel::clearError)

                        // Filter Chips
                        AutoScrollingFilterRow(
                            contentPadding = PaddingValues(bottom = 4.dp),
                            items = DashboardCategory.entries,
                            selectedItem = selectedCategory,
                            onSelectionChanged = { selectedCategory = it },
                            labelProvider = { it.label() },
                        )

                        // Dashboard List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            displayedCategories.forEach { category ->
                                val tiles = groupedTiles[category]
                                if (!tiles.isNullOrEmpty()) {
                                    stickyHeader(key = "header_${category.name}") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.background)
                                                .padding(vertical = 8.dp),
                                        ) {
                                            Text(
                                                text = category.label(),
                                                color = primaryColor,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }

                                    items(
                                        items = tiles,
                                        key = { it.id },
                                    ) { dashItem ->
                                        val delay = delays[dashItem.id] ?: 0
                                        LongTile(dashItem, delay = delay, shouldAnimate = shouldAnimate)
                                    }
                                }
                            }

                            // Bottom Spacer
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }

                    // Bottom bars (offscreen, available for shared transitions)
                    DualActionBottomBar(
                        mainActionText = stringResource(string.career_plan_edit),
                        mainActionIcon = Icons.Default.Edit,
                        onMainActionClick = {},
                        secondaryActionIcon = Icons.Default.Print,
                        onSecondaryActionClick = {},
                        footerText = stringResource(
                            string.career_plan_last_modified,
                            studyPlanLastUpdated ?: "-"
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 230.dp)
                    )
                }
            }
        }

        val prefs = rememberPreferencesManager()
        val lastModifiedTime = prefs.studyPlanLastModified
        val lastModifiedStr = remember(lastModifiedTime) {
            if (lastModifiedTime == 0L) null
            else java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(lastModifiedTime))
        }

        // Bottom bars (offscreen, available for shared transitions)
        DualActionBottomBar(
            mainActionText = stringResource(string.career_plan_edit),
            mainActionIcon = Icons.Default.Edit,
            onMainActionClick = { /* nothing */ },
            secondaryActionIcon = Icons.Default.Print,
            onSecondaryActionClick = { /* nothing */ },
            footerText = lastModifiedStr?.let {
                stringResource(
                    string.career_plan_last_modified,
                    it
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 230.dp)
        )

        SingleActionBottomBar(
            text = "Stampa importo Tasse dovute",
            icon = Icons.Default.Print,
            onClick = {
                haptic.tap()
                // TODO: implement pdf opening
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 230.dp)
        )
    }
}


@Composable
fun LongTile(
    item: DashboardTile,
    delay: Int = 0,
    shouldAnimate: Boolean = true,
) {
    val titleColor = when (item.status) {
        DashboardTileStatus.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        DashboardTileStatus.Important -> MaterialTheme.colorScheme.onBackground
        else -> MaterialTheme.colorScheme.onPrimary
    }
    val subtitleColor = MaterialTheme.colorScheme.onBackground
    val haptic = rememberHapticManager()

    val randomX = remember { Random.nextInt(-10, 5).dp }
    val randomRotation = remember { Random.nextInt(-5, 5).toFloat() }

    val heightAnim = remember { Animatable(item.heightDp, Dp.VectorConverter) }

    LaunchedEffect(Unit) {
        if (shouldAnimate) {
            delay(delay.toLong())
            heightAnim.animateTo(
                targetValue = item.heightDp * 1.05f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            )
            heightAnim.animateTo(
                targetValue = item.heightDp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }

    SimpleCard(
        modifier = Modifier.height(heightAnim.value),
        backgroundColor = when (item.status) {
            DashboardTileStatus.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
            DashboardTileStatus.Important -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainerLowest
        },
        onClick = {
            haptic.tap()
            item.action()
        },
        background = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = titleColor,
                modifier = Modifier
                    .size(75.dp - (if (item.subtitle == null) 15 else 0).dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-45).dp + randomX)
                    .rotate(randomRotation)
                    .alpha(0.2f),
            )
        },
        leading = {
            val borderColor = when (item.status) {
                DashboardTileStatus.Warning -> MaterialTheme.colorScheme.tertiaryContainer
                DashboardTileStatus.Important -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .width(7.dp)
                    .fillMaxHeight()
                    .background(borderColor),
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 10.dp)
                    .align(Alignment.TopStart),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = titleColor.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                    )

                    if (item.status == DashboardTileStatus.Warning) {
                        val borderColor = MaterialTheme.colorScheme.tertiaryContainer

                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            tint = borderColor,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (item.subtitle != null) Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                )
            }

            // "Open" Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = titleColor,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 5.dp),
            )
        }
    }
}
