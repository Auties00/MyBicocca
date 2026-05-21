package it.attendance100.mybicocca.ui.screen.segreterie.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.ui.component.EmptyOfflineState
import it.attendance100.mybicocca.ui.component.EmptyState
import it.attendance100.mybicocca.ui.component.ErrorState
import it.attendance100.mybicocca.ui.component.SingleActionBottomBar
import it.attendance100.mybicocca.ui.component.StatusBar
import it.attendance100.mybicocca.ui.component.card.SimpleCard
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonBookingCard
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonCardList
import it.attendance100.mybicocca.ui.navigation.LocalSharedTransitionScope
import it.attendance100.mybicocca.util.shared_transitions.CommonSharedElementKey
import it.attendance100.mybicocca.util.shared_transitions.CommonSharedElementType
import it.attendance100.mybicocca.util.shared_transitions.ExamSessionSharedElementType
import it.attendance100.mybicocca.util.shared_transitions.ExamSessionsElementKey
import it.attendance100.mybicocca.util.shared_transitions.bicoccaSharedElement
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: BookingViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val examCalls by viewModel.examCalls.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val error by viewModel.error.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    Box {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            indicator = {},
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                isRefreshing -> {
                    Column {
                        StatusBar(
                            isOnline = isOnline,
                            errorMessage = error,
                            onDismissError = viewModel::clearError
                        )
                        SkeletonCardList(spacing = 16.dp) { shimmer ->
                            SkeletonBookingCard(shimmerInstance = shimmer)
                        }
                    }
                }

                examCalls.isEmpty() && !isOnline -> EmptyOfflineState()
                examCalls.isEmpty() && error != null -> ErrorState(message = error ?: "Errore")
                examCalls.isEmpty() -> EmptyState(message = "Nessun appello disponibile")

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            StatusBar(
                                isOnline = isOnline,
                                errorMessage = error,
                                onDismissError = viewModel::clearError
                            )
                        }

                        items(
                            items = examCalls,
                            key = { it.id },
                        ) { examCall ->
                            BookingExamCard(
                                examCall = examCall,
                                onClick = { onNavigateToDetail(examCall.id) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }

        with(LocalSharedTransitionScope.current!!) {
            SingleActionBottomBar(
                text = "Prenota Appello",
                icon = Icons.Default.EventAvailable,
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 128.dp)
                    .renderInSharedTransitionScopeOverlay(100f)
                    .bicoccaSharedElement(
                        key = CommonSharedElementKey(
                            CommonSharedElementKey.EXAM_SESSIONS_KEY,
                            CommonSharedElementType.BottomActionBar
                        )
                    ),
            )
        }
    }
}

@Composable
fun BookingExamCard(
    examCall: ExamCall,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SimpleCard(
        modifier = modifier
            .fillMaxWidth()
            .bicoccaSharedElement(
                key = ExamSessionsElementKey(
                    examCall.id.toString(),
                    ExamSessionSharedElementType.Card
                ),
                zIndexInOverlay = -1f,
                clipShape = RoundedCornerShape(16.dp),
            ),
        ditherImage = null,
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                // Course Name
                Text(
                    text = examCall.activityName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bicoccaSharedElement(
                            key = ExamSessionsElementKey(
                                examCall.id.toString(),
                                ExamSessionSharedElementType.Title
                            ),
                        ),
                )

                // Description (cleaned)
                val displayDescription = remember(examCall.activityName, examCall.activityCode) {
                    examCall.activityCode?.let { code ->
                        cleanDescription(examCall.activityName, code)
                    }
                }
                if (displayDescription != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = displayDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date and location info chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val dateText = examCall.date?.format(
                        DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
                    )
                    val timeText = examCall.startTime?.format(
                        DateTimeFormatter.ofPattern("HH:mm")
                    )
                    val dateTimeText = listOfNotNull(dateText, timeText).joinToString(" ")

                    if (dateTimeText.isNotBlank()) {
                        InfoChip(
                            icon = Icons.Outlined.CalendarMonth,
                            text = dateTimeText,
                        )
                    }
                    val building = examCall.building
                    if (!building.isNullOrBlank()) {
                        InfoChip(
                            icon = Icons.Outlined.LocationOn,
                            text = building.take(15),
                        )
                    }
                }

                // Exam state description row
                examCall.stateDescription?.let { state ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Extra details with AnimatedVisibility
                val hasExtraDetails =
                    (!examCall.building.isNullOrBlank() && !examCall.room.isNullOrBlank()) ||
                            examCall.enrolledCount != null

                AnimatedVisibility(
                    visible = hasExtraDetails,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Column {
                        // Location
                        val room = examCall.room
                        val building = examCall.building
                        if (!building.isNullOrBlank() || !room.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = listOfNotNull(building, room).joinToString(" - "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Registration count if available
                        examCall.enrolledCount?.let { count ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$count iscritti",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun InfoChip(
    icon: ImageVector,
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}


private fun cleanDescription(name: String, description: String): String? {
    val n = name.lowercase().trim()
    val d = description.lowercase().trim()

    // Exact Match or Name starts with Description (description contains less or equal info than name)
    if (n == d || n.startsWith(d)) return null

    // Description starts with Name
    if (d.startsWith(n)) {
        val remaining = description.substring(name.length)
        // Trim left non-alphanumerical characters
        val trimmed = remaining.dropWhile { !it.isLetterOrDigit() }
        return trimmed.ifBlank { null }
    }

    // Completely specific description retained
    return description
}
