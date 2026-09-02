package it.attendance100.mybicocca.core.version

import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.domain.model.update.AppRelease

/** Whether this running build is itself a nightly build. */
val isNightlyBuild: Boolean = BuildConfig.VERSION_NAME.contains("nightly", ignoreCase = true)

/**
 * Whether the release identified by [commitSha]/[versionName] is the build already running.
 *
 * Nightlies are matched on the commit, because every nightly built against the same base version
 * shares a versionCode *and* a versionName — the commit is the only thing that distinguishes them.
 * Stable releases carry no commit, so they fall back to the version, with the running build's
 * `-nightly` suffix stripped.
 *
 * Worth checking wherever an "update available" flag is acted on: nothing clears that flag when
 * the user installs the release, so it stays true for a build that is now the running one.
 */
fun isRunningBuild(commitSha: String?, versionName: String): Boolean =
    if (!commitSha.isNullOrBlank()) {
        commitSha == BuildConfig.COMMIT_SHA
    } else {
        versionName == BuildConfig.VERSION_NAME.substringBefore("-")
    }

fun AppRelease.isRunningBuild(): Boolean = isRunningBuild(commitSha, versionName)
