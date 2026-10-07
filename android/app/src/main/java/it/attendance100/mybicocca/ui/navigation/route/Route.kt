package it.attendance100.mybicocca.ui.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Every destination of [MainShell]'s back stack: a full-screen page ([AppRoute]) or a modal sheet
 * page ([SheetRoute]). Sealed so a [Destination] can carry any route through kotlinx
 * serialization without a polymorphic module, which is what lets the whole stack — open sheets
 * and their in-sheet pages included — survive process death.
 */
@Serializable
sealed interface Route : NavKey
