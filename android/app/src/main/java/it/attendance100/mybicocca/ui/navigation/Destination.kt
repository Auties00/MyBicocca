package it.attendance100.mybicocca.ui.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import it.attendance100.mybicocca.ui.navigation.route.Route
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * One back-stack slot: a [route] plus the [id] of this particular push. Routes are value types —
 * the ISEE sheet is always `SheetRoute.Isee`, a course is always `CourseDetail(42)` — so two pushes
 * of the same route are indistinguishable by the route alone. Navigation3 keys every per-entry
 * state holder (saveable state, ViewModel store, scene movable content) by the entry's contentKey,
 * and two live entries sharing one ends in `IllegalArgumentException: Key … was used multiple
 * times` (issue #44). The id makes every push unique, exactly like Navigation2's
 * NavBackStackEntry.id: it is the contentKey of the entry, the identity of the sheet a modal page
 * opens, and the handle [AppNavigator] pops by, so a stale pop can never remove the wrong entry.
 *
 * Serializable (the id included) so the whole stack is restored after process death with the same
 * identities the saved per-entry state was filed under.
 */
@Serializable
data class Destination(
    val route: Route,
    val id: String = UUID.randomUUID().toString(),
) : NavKey

/**
 * Adapts a route-keyed entry provider (the `entryProvider { entry<Route> { … } }` DSL) to the
 * [Destination] back stack: the route's entry keeps its metadata and content, while the
 * destination's unique id becomes the contentKey every per-entry state holder is filed under. The
 * destination itself rides along in the metadata ([NavEntry.destination]) because NavEntry keeps
 * its key private and scenes need the route (e.g. to resolve a sheet page's header).
 */
fun destinationEntryProvider(
    routeProvider: (NavKey) -> NavEntry<NavKey>,
): (NavKey) -> NavEntry<NavKey> = { key ->
    val destination = key as Destination
    val routeEntry = routeProvider(destination.route)
    NavEntry(
        key = key,
        contentKey = destination.id,
        metadata = routeEntry.metadata + (DestinationMetadataKey to destination),
    ) {
        CompositionLocalProvider(LocalDestination provides destination) { routeEntry.Content() }
    }
}

/**
 * The [Destination] whose entry is being composed — lets an entry address itself (e.g. a detail
 * page popping only itself when its item disappears) without depending on what is on top.
 */
val LocalDestination = staticCompositionLocalOf<Destination?> { null }

private const val DestinationMetadataKey = "it.attendance100.mybicocca.destination"

/** The [Destination] behind an entry built by [destinationEntryProvider]. */
val NavEntry<*>.destination: Destination
    get() = metadata[DestinationMetadataKey] as? Destination
        ?: error("Entry $contentKey was not built by destinationEntryProvider")
