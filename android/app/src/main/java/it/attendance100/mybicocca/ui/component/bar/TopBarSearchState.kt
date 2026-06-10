package it.attendance100.mybicocca.ui.component.bar

import androidx.compose.runtime.Immutable

/** Hoisted state and callbacks for the top bar's search mode: the shell owns the values, the bar renders them. */
@Immutable
data class TopBarSearchState(
    val query: String,
    val active: Boolean,
    /** Dictation in progress: the mic tints primary (the session itself lives in its dialog). */
    val dictating: Boolean,
    val onQueryChange: (String) -> Unit,
    val onActiveChange: (Boolean) -> Unit,
    /** Mic tap while the field is empty (starts/stops dictation). */
    val onMicClick: () -> Unit,
    /** IME search action: commits to history (+ AI interpretation) while keeping the search view open. */
    val onSubmit: () -> Unit,
)
