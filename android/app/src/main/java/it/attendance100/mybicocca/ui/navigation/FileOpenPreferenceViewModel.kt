package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import it.attendance100.mybicocca.domain.usecase.settings.ClearFileOpenChoiceUseCase
import it.attendance100.mybicocca.domain.usecase.settings.ObserveFileOpenChoicesUseCase
import it.attendance100.mybicocca.domain.usecase.settings.SetFileOpenChoiceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes the remembered per-kind file-open choices to the shell, and persists new ones. */
@HiltViewModel
class FileOpenPreferenceViewModel @Inject constructor(
    observeFileOpenChoices: ObserveFileOpenChoicesUseCase,
    private val setFileOpenChoice: SetFileOpenChoiceUseCase,
    private val clearFileOpenChoice: ClearFileOpenChoiceUseCase,
) : ViewModel() {

    /** Remembered open choices keyed by a kind's preference key; kinds absent ask every time. */
    val choices: StateFlow<Map<String, FileOpenChoice>> = observeFileOpenChoices()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun remember(kind: String, choice: FileOpenChoice) {
        viewModelScope.launch { setFileOpenChoice(kind, choice) }
    }

    /**
     * Drops a remembered choice so the chooser is shown again next time (the "Chiedi ogni volta"
     * pick in the file-associations settings).
     */
    fun forget(kind: String) {
        viewModelScope.launch { clearFileOpenChoice(kind) }
    }
}
