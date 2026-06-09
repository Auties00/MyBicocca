package it.attendance100.mybicocca.ui.screen.registry.subscreen.titles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.document.AcademicTitle
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.document.GetAcademicTitlesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TitlesViewModel @Inject constructor(
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val getAcademicTitles: GetAcademicTitlesUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _titles = MutableStateFlow<Loadable<List<AcademicTitle>>>(Loadable.NotYetLoaded)
    val titles: StateFlow<Loadable<List<AcademicTitle>>> = _titles.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().distinctUntilChanged().collect { load(it) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val id = activeCareerId.filterNotNull().first()
            load(id)
        }
    }

    private suspend fun load(careerId: CareerId) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { getAcademicTitles(careerId) }
            .onSuccess {
                _titles.value = Loadable.Loaded(it)
                _syncStatus.value = SyncStatus.Idle
            }
            .onFailure { _syncStatus.value = SyncStatus.Failed(it) }
    }
}
