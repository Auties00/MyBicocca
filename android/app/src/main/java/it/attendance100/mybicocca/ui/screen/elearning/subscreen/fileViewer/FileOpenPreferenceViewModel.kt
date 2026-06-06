package it.attendance100.mybicocca.ui.screen.elearning.subscreen.fileViewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.local.settings.FileOpenChoice
import it.attendance100.mybicocca.data.local.settings.FileOpenPreferenceStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Exposes the remembered per-kind file-open choices to the shell, and persists new ones.
@HiltViewModel
class FileOpenPreferenceViewModel @Inject constructor(
    private val store: FileOpenPreferenceStore,
) : ViewModel() {

    val choices: StateFlow<Map<String, FileOpenChoice>> = store.choices
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun remember(kind: String, choice: FileOpenChoice) {
        viewModelScope.launch { store.setChoice(kind, choice) }
    }
}
