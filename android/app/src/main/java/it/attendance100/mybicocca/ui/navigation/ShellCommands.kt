package it.attendance100.mybicocca.ui.navigation

import it.attendance100.mybicocca.ui.navigation.route.Route
import it.attendance100.mybicocca.ui.navigation.route.ShellTab
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Puts the shell on [tab] with exactly [routes] stacked on top of the tab root, in order. The map
 * keeps its sheets outside the back stack, so [buildingsList] and [building] open those instead.
 */
data class ShellCommand(
    val tab: ShellTab,
    val routes: List<Route>,
    val buildingsList: Boolean = false,
    val building: String? = null,
)

/**
 * Cross-shell navigation bus used both by feature sheets (for example, jumping from the Biblioteca
 * sheet straight to a building on the campus map) and by the demo build's screenshot capture.
 */
object ShellCommands {
    private val commands = MutableSharedFlow<ShellCommand>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val flow: SharedFlow<ShellCommand> = commands

    /** The routes currently on the shell's back stack, bottom first, so a sender can check where it landed. */
    @Volatile
    var stack: List<Route> = emptyList()

    fun send(command: ShellCommand) {
        commands.tryEmit(command)
    }
}
