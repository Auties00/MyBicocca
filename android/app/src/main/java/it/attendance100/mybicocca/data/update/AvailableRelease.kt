package it.attendance100.mybicocca.data.update

import it.attendance100.mybicocca.core.version.isRunningBuild
import it.attendance100.mybicocca.data.local.settings.PersistedNightlyState
import it.attendance100.mybicocca.data.local.settings.PersistedUpdateState
import it.attendance100.mybicocca.domain.model.update.AppRelease

/**
 * The release worth acting on, or null.
 *
 * Installing an update never clears the stored "available" flag, so it stays set for the build
 * that is now running; acting on the flag alone re-offers the update the user just installed.
 * This is the same reconciliation `observeNightlyStatus` applies before reporting a status to
 * the UI, and every path that reads the stored state has to apply it too.
 */
internal fun PersistedUpdateState.availableRelease(): AppRelease? =
    release?.takeIf { available && !it.isRunningBuild() }

internal fun PersistedNightlyState.availableRelease(): AppRelease? =
    release?.takeIf { available && !it.isRunningBuild() }
