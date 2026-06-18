package it.attendance100.mybicocca.ui.screen.settings.subscreen.appInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.update.AppRelease
import it.attendance100.mybicocca.domain.usecase.update.GetReleasesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the "What's New" sheet: loads the published releases once when the sheet opens and
 * exposes a [state] the UI maps to a loading spinner, an error message with [retry], an empty
 * state, or the release list.
 */
@HiltViewModel
class WhatsNewViewModel @Inject constructor(
    private val getReleases: GetReleasesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<WhatsNewUiState>(WhatsNewUiState.Loading)
    val state: StateFlow<WhatsNewUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = WhatsNewUiState.Loading
        viewModelScope.launch {
            _state.value = runCatching { getReleases() }
                .fold(
                    onSuccess = { WhatsNewUiState.Loaded(it) },
                    onFailure = { WhatsNewUiState.Error },
                )
        }
    }
}

/** UI state for the "What's New" sheet. A [Loaded] with an empty list is the genuine empty state. */
sealed interface WhatsNewUiState {
    data object Loading : WhatsNewUiState
    data object Error : WhatsNewUiState
    data class Loaded(val releases: List<AppRelease>) : WhatsNewUiState
}
