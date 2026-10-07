package it.attendance100.mybicocca.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey

/*
 * Effects for the content of a shell back-stack entry: reacting to the entry leaving the stack and
 * closing it, always addressed by the entry's own id.
 */

/**
 * Per-entry "left the back stack" actions registered by [DisposableEffectOnPop]. Navigation3 calls
 * a decorator's onPop for every popped entry — when its content leaves composition, or right away
 * if it was not composed at the time — so the actions run exactly once per pop, including for a
 * sheet that was hidden under a full-screen page when the stack was cleared.
 */
class EntryPopActions internal constructor() {
    private val actions = HashMap<Any, MutableMap<Long, () -> Unit>>()

    internal fun register(contentKey: Any, slot: Long, action: () -> Unit) {
        actions.getOrPut(contentKey) { HashMap() }[slot] = action
    }

    internal fun onPop(contentKey: Any) {
        actions.remove(contentKey)?.values?.forEach { it() }
    }
}

private val LocalEntryPopActions = staticCompositionLocalOf<EntryPopActions?> { null }

/** The shell's entry decorator backing [DisposableEffectOnPop]. */
@Composable
fun rememberEntryPopActionsDecorator(): NavEntryDecorator<NavKey> {
    val actions = remember { EntryPopActions() }
    return remember(actions) {
        NavEntryDecorator(onPop = actions::onPop) { entry ->
            CompositionLocalProvider(LocalEntryPopActions provides actions) { entry.Content() }
        }
    }
}

/**
 * Runs [onPopped] once the calling entry is removed from the back stack — and NOT when its content
 * merely stops being shown while the entry stays on the stack (a sheet sliding away under a
 * full-screen page pushed from it, e.g. a PDF, comes back with its flow intact). Outside a shell
 * entry (a standalone UI test) it runs when the content is disposed.
 *
 * Call it unconditionally at the entry's top level: the action is filed under its call site and
 * stays registered while the entry is hidden, so a call site that comes and goes would leave a
 * stale action behind.
 */
@Composable
fun DisposableEffectOnPop(onPopped: () -> Unit) {
    val actions = LocalEntryPopActions.current
    val self = LocalDestination.current
    val latest = rememberUpdatedState(onPopped)
    val slot = currentCompositeKeyHashCode
    DisposableEffect(actions, self, slot) {
        // Stays registered after disposal on purpose: the pop may come later.
        if (actions != null && self != null) actions.register(self.id, slot) { latest.value() }
        onDispose {
            if (actions == null || self == null) latest.value()
        }
    }
}

/**
 * Pops the calling entry (and anything above it) — the safe "close this page" action: addressed by
 * the entry's own id, it can neither pop an unrelated entry nor run twice.
 */
@Composable
fun rememberPopSelf(): () -> Unit {
    val navigator = requireAppNavigator()
    val self = checkNotNull(LocalDestination.current) { "rememberPopSelf outside a shell entry" }
    return remember(navigator, self) { { navigator.pop(self.id) } }
}

/**
 * Closes a detail page whose item disappeared from its list (e.g. evicted by a career switch),
 * with three guards:
 * - only once the list has actually loaded: right after a process-death restore the page is back
 *   on the stack before its data is, and popping then would throw away the very page the user was on;
 * - only while the page is on top: a sheet stacked above it (e.g. the booking manager opened from a
 *   calendar event, where cancelling the booking removes the event) must not be torn down with it —
 *   the page closes once it is back on top;
 * - with [onlyAfterSeen], only after the item was present at least once, for lists that can briefly
 *   hold a previous query's rows (the calendar month list right after a month switch).
 */
@Composable
fun PopWhenMissing(loaded: Boolean, missing: Boolean, onlyAfterSeen: Boolean = false) {
    val navigator = requireAppNavigator()
    val self = checkNotNull(LocalDestination.current) { "PopWhenMissing outside a shell entry" }
    var seen by rememberSaveable { mutableStateOf(false) }
    if (!missing) seen = true
    val onTop by remember(navigator, self) { derivedStateOf { navigator.top.id == self.id } }
    val shouldPop = loaded && missing && onTop && (seen || !onlyAfterSeen)
    LaunchedEffect(shouldPop) {
        if (shouldPop) navigator.pop(self.id)
    }
}
