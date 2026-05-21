package it.attendance100.mybicocca.ui.component.bar

import androidx.compose.runtime.Immutable

@Immutable
data class TopBarSearchState(
    val query: String,
    val active: Boolean,
    val placeholder: String,
    val onQueryChange: (String) -> Unit,
    val onActiveChange: (Boolean) -> Unit,
)
