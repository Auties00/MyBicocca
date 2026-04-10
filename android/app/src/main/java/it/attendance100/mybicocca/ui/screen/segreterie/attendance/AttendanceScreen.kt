package it.attendance100.mybicocca.ui.screen.segreterie.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.data.model.attendance.AttendanceRecord
import it.attendance100.mybicocca.ui.component.EmptyOfflineState
import it.attendance100.mybicocca.ui.component.EmptyState
import it.attendance100.mybicocca.ui.component.ErrorState
import it.attendance100.mybicocca.ui.component.NetworkStatusBar
import it.attendance100.mybicocca.ui.component.card.SimpleCard
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonAttendanceCard
import it.attendance100.mybicocca.ui.component.shimmer.SkeletonCardList
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

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
                    SkeletonCardList { shimmer ->
                        SkeletonAttendanceCard(shimmerInstance = shimmer)
                    }
                }
            }

            records.isEmpty() && !isOnline -> EmptyOfflineState()
            records.isEmpty() && error != null -> ErrorState(message = error ?: "Errore")
            records.isEmpty() -> EmptyState(message = "Nessun dato presenze disponibile")

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        NetworkStatusBar(isOnline = isOnline, errorMessage = error, onDismissError = viewModel::clearError)
                    }

                    items(
                        items = records,
                        key = { it.id },
                    ) { record ->
                        AttendanceCard(
                            record = record,
                            modifier = Modifier.animateItem(),
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AttendanceCard(
    record: AttendanceRecord,
    modifier: Modifier = Modifier,
) {
    val progress = (record.attendancePercentage / 100f).coerceIn(0f, 1f)

    SimpleCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = record.courseName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = record.courseCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AttendanceInfo(
                    icon = Icons.Outlined.CheckCircle,
                    text = String.format(
                        Locale.getDefault(),
                        "%.0f%% presenti",
                        record.attendancePercentage,
                    ),
                    modifier = Modifier.weight(1f),
                )
                AttendanceInfo(
                    icon = Icons.Outlined.Schedule,
                    text = "${record.lessonsAttended} lezioni",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AttendanceInfo(
                    icon = Icons.Outlined.Info,
                    text = String.format(Locale.getDefault(), "%.1f ore", record.totalHours),
                    modifier = Modifier.weight(1f),
                )
                AttendanceInfo(
                    icon = Icons.Outlined.Info,
                    text = record.status,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AttendanceInfo(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
