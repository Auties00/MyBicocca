package it.attendance100.mybicocca.ui.component.bar

import androidx.compose.runtime.Immutable

@Immutable
data class TopBarSearchState(
    val query: String,
    val active: Boolean,
    // Dictation in progress: the mic tints primary (the session itself lives in its dialog).
    val dictating: Boolean,
    val onQueryChange: (String) -> Unit,
    val onActiveChange: (Boolean) -> Unit,
    // Mic tap while the field is empty (starts/stops dictation).
    val onMicClick: () -> Unit,
    // IME search action: commit to history (+ AI interpretation). Keeps the view open.
    val onSubmit: () -> Unit,
)
