package it.attendance100.mybicocca.ui.screen.segreterie.studyplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.ui.component.DualActionBottomBar
import it.attendance100.mybicocca.util.rememberHapticManager
import it.attendance100.mybicocca.util.rememberPreferencesManager

@Composable
fun PianoCarrieraScreen(
    onNavigateToEdit: (studentId: Long, choiceRegulationId: Long, schemaId: Long, planId: Long) -> Unit = { _, _, _, _ -> },
    viewModel: PianoCarrieraViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val headers by viewModel.headers.collectAsStateWithLifecycle()
    val isEditEnabled by viewModel.isEditEnabled.collectAsStateWithLifecycle()

    // Group by year
    val groupedPlan = remember(courses) {
        courses.groupBy { it.year ?: 0 }
    }
    val years = remember(groupedPlan) { groupedPlan.keys.sorted() }

    // State for expanded sections
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(years) {
        years.forEach { year ->
            if (!expandedStates.containsKey(year)) {
                expandedStates[year] = true
            }
        }
    }

    val haptic = rememberHapticManager()
    val listState = rememberLazyListState()
    var isBottomBarVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -0.5 && listState.canScrollForward) {
                    isBottomBarVisible = false
                } else if (available.y > 0.5) {
                    isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(listState.canScrollForward) {
        if (!listState.canScrollForward) {
            isBottomBarVisible = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(bottom = 140.dp, top = 16.dp),
        ) {
            // Plan info card
            item {
                val headerDescription = headers.firstOrNull()?.description ?: ""
                val headerStatus = headers.firstOrNull()?.statusDescription
                    ?: stringResource(R.string.career_plan_status_value)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    onClick = {
                        haptic.tap()
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = 12.dp
                            ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.career_plan_type_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = headerDescription.ifBlank { stringResource(R.string.career_plan_type_value) },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Box(
                                modifier = Modifier.height(35.dp),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Text(
                                    text = headerStatus,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            years.forEach { year ->
                stickyHeader {
                    PianoExpandableHeader(
                        title = "${stringResource(R.string.career_plan_year_prefix)} $year",
                        count = groupedPlan[year]?.size ?: 0,
                        isExpanded = expandedStates[year] == true,
                        titleColor = MaterialTheme.colorScheme.primary,
                        onToggle = {
                            expandedStates[year] = !(expandedStates[year] ?: true)
                        },
                    )
                }

                if (expandedStates[year] == true) {
                    items(groupedPlan[year] ?: emptyList()) { item ->
                        PlannedCourseCard(item = item)
                    }
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

        // Bottom action bar
        DualActionBottomBar(
            mainActionText = stringResource(R.string.career_plan_edit),
            mainActionIcon = Icons.Default.Edit,
            mainIsEnabled = isEditEnabled,
            onMainActionClick = {
                haptic.tap()
                val header = headers.firstOrNull() ?: return@DualActionBottomBar
                val regId = header.choiceRegulationId ?: return@DualActionBottomBar
                val sId = header.schemaId ?: return@DualActionBottomBar
                onNavigateToEdit(header.studentId, regId, sId, header.id)
            },
            secondaryActionIcon = Icons.Default.Print,
            onSecondaryActionClick = {
                haptic.tap()
                /* TODO: implement print */
            },
            footerText = lastModifiedStr?.let {
                stringResource(
                    R.string.career_plan_last_modified,
                    it
                )
            },
            isBottomBarVisible = isBottomBarVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}


@Composable
fun PianoExpandableHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    titleColor: Color,
    onToggle: () -> Unit,
) {
    val haptic = rememberHapticManager()
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.tap()
                onToggle()
            },
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                PianoCountBadge(count = count)
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.expand),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
fun PianoCountBadge(count: Int) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Text(
            text = count.toString(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}


@Composable
fun PlannedCourseCard(item: PlannedCourse) {
    val haptic = rememberHapticManager()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = {
            haptic.tap()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.activityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                val status = item.statusDescription ?: ""
                val containerColor =
                    if (status.equals("Superata", ignoreCase = true))
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                val contentColor =
                    if (status.equals("Superata", ignoreCase = true))
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSecondaryContainer

                if (status.isNotBlank()) {
                    Surface(
                        modifier = Modifier.width(80.dp),
                        color = containerColor,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Code + CFU Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = item.activityCode ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier.offset(x = (-24).dp),
                ) {
                    Text(
                        text = "${item.credits.toInt()} CFU",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
