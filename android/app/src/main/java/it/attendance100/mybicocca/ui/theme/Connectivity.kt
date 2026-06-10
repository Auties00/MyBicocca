package it.attendance100.mybicocca.ui.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * App-wide connectivity state, provided around the whole app by AppRoot from RootViewModel.
 * Buttons whose action needs the network read this to disable themselves while offline;
 * retry and refresh affordances deliberately stay enabled so the user can re-check on their own.
 */
val LocalIsOnline = compositionLocalOf { true }
