package it.attendance100.mybicocca.core.os

import androidx.compose.runtime.compositionLocalOf

/**
 * A CompositionLocal to signal that the composable is running within a test environment.
 * Useful for disabling hardware-specific behavior (e.g. sensors) that would fail or block
 * in tests without providing heavy fakes.
 */
val LocalIsTestEnvironment = compositionLocalOf { false }
