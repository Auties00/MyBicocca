package it.attendance100.mybicocca.data.update

import it.attendance100.mybicocca.core.version.SemVer
import it.attendance100.mybicocca.core.version.isNewerThanRunningBuild
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

/**
 * The nightly counterpart, judged by version rather than identity: nightlies are ordered by
 * [isNewerThanRunningBuild] (`0.0.6-nightly.12` < `…13`), which also rules out the running one.
 *
 * Only a version that reads as a pre-release counts. A release stored by a build from before
 * per-build nightlies carries its localized publish date where the version should be, and in a
 * dotted-date locale that parses as a large *final* version — `29.08.2026, 14:00` reads as 29.8.0
 * — which would outrank everything and offer the old rolling build. The next check replaces it.
 */
internal fun PersistedNightlyState.availableRelease(): AppRelease? =
    release?.takeIf {
        available && SemVer.parse(it.versionName)?.isPreRelease == true &&
            isNewerThanRunningBuild(it.versionName)
    }
