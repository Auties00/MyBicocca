package it.attendance100.mybicocca.ui.screen.registry

/**
 * Stable `testTag` identifiers for [RegistryScreen], referenced by both the screen and its UI tests
 * so a user-visible copy change never breaks a test and a tag rename is a compile error rather than
 * a silently missed node. The screen applies them with `Modifier.testTag`; tests anchor on the
 * constants instead of on Italian text.
 *
 * The directory tiles are addressed by their stable [it.attendance100.mybicocca.ui.screen.registry.state.RegistryService.id]
 * via [service]; the scadenze banner and the service grid get a single coarse marker each.
 */
object RegistryTestTags {
    const val ROOT = "registry:root"
    const val SCADENZE_HEADER = "registry:scadenzeHeader"
    const val SERVICES = "registry:services"

    /** Per-service directory tile, keyed by the service id (e.g. `registry:service:taxes`). */
    fun service(id: String): String = "registry:service:$id"
}
