package it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import it.attendance100.mybicocca.domain.usecase.transcript.ObserveTranscriptRowsUseCase
import it.attendance100.mybicocca.domain.usecase.transcript.RefreshTranscriptUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.state.ChartPoint
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.state.TranscriptPartition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import it.attendance100.mybicocca.ui.navigation.AppRoute

@HiltViewModel(assistedFactory = TranscriptViewModel.Factory::class)
class TranscriptViewModel @AssistedInject constructor(
    @Assisted private val key: AppRoute.Transcript,
    savedState: SavedStateHandle,
    observeRowsUseCase: ObserveTranscriptRowsUseCase,
    private val refreshTranscript: RefreshTranscriptUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: AppRoute.Transcript): TranscriptViewModel
    }

    val careerId: CareerId = CareerId(key.careerId)

    val rows: StateFlow<Loadable<List<TranscriptRow>>> = observeRowsUseCase(careerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    // Split + sort + chart-series derivation runs ONCE per Room emission, off the main
    // thread. Composables read these terminal flows directly — no recompose-time work.
    val partition: StateFlow<Loadable<TranscriptPartition>> = rows
        .map { loadable ->
            when (loadable) {
                Loadable.NotYetLoaded -> Loadable.NotYetLoaded
                is Loadable.Loaded -> Loadable.Loaded(loadable.value.partition())
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val chartSeries: StateFlow<Loadable<List<ChartPoint>>> = rows
        .map { loadable ->
            when (loadable) {
                Loadable.NotYetLoaded -> Loadable.NotYetLoaded
                is Loadable.Loaded -> Loadable.Loaded(loadable.value.toChartSeries())
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        viewModelScope.launch { runRefresh(force = false) }
    }

    fun refresh() {
        viewModelScope.launch { runRefresh(force = true) }
    }

    private suspend fun runRefresh(force: Boolean) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshTranscript(careerId, force) }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure { _syncStatus.value = SyncStatus.Failed(it) }
    }

    private companion object {
        const val KEEP_ALIVE_MS = 5_000L
    }
}

private fun List<TranscriptRow>.partition(): TranscriptPartition {
    val passed = ArrayList<TranscriptRow>(size)
    val pending = ArrayList<TranscriptRow>(size)
    for (row in this) {
        when (row.state) {
            TranscriptRowState.Passed -> passed.add(row)
            TranscriptRowState.Frequented, TranscriptRowState.Planned -> pending.add(row)
        }
    }
    return TranscriptPartition(passed = passed, pending = pending)
}

private fun List<TranscriptRow>.toChartSeries(): List<ChartPoint> {
    // Only passed rows with a numeric grade and an exam date contribute to the chart.
    // 30L (cum laude) is normalized to 31 for plotting; the axis formatter renders it back as "30L".
    val points = ArrayList<ChartPoint>(size)
    var index = 0
    for (row in this) {
        if (row.state != TranscriptRowState.Passed) continue
        val grade = row.grade ?: continue
        val date = row.examDate ?: continue
        val plotted = if (row.cumLaude) 31 else grade
        points.add(ChartPoint(rowId = row.id, x = index.toFloat(), y = plotted.toFloat(), date = date))
        index++
    }
    return points
}

// Suppress unused; helper for future ranking work.
@Suppress("unused")
private fun Loadable<*>.touchValue(): Any? = valueOrNull()
