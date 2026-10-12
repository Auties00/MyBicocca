package it.attendance100.mybicocca.core.version

import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.domain.model.update.AppRelease

/** Whether this running build is itself a nightly build. */
val isNightlyBuild: Boolean = BuildConfig.VERSION_NAME.contains("nightly", ignoreCase = true)

/**
 * Whether the release identified by [commitSha]/[versionName] is the build already running.
 *
 * Nightlies are matched on the commit when the release carries one. Each nightly now has a version
 * name of its own (`0.0.6-nightly.12`, N counting commits since the last stable tag), so the name
 * alone would mostly do, but the commit is exact even for a build whose N couldn't be counted (a
 * local build without the tag, or one from before per-build nightlies, all named `0.0.6-nightly`).
 * Stable releases carry no commit, so they fall back to the version name, compared whole: a
 * nightly is named after the release it is heading towards, and stripping the suffix would make
 * it mistake that release for itself the day it ships.
 *
 * Worth checking wherever an "update available" flag is acted on: nothing clears that flag when
 * the user installs the release, so it stays true for a build that is now the running one.
 */
fun isRunningBuild(commitSha: String?, versionName: String): Boolean =
    isSameBuild(commitSha, versionName, BuildConfig.COMMIT_SHA, BuildConfig.VERSION_NAME)

internal fun isSameBuild(
    commitSha: String?,
    versionName: String,
    runningCommitSha: String,
    runningVersionName: String,
): Boolean =
    if (!commitSha.isNullOrBlank()) commitSha == runningCommitSha
    else versionName == runningVersionName

/**
 * Whether the release [versionName] is newer than the build running.
 *
 * The running version name goes in whole, suffix included: [SemVer] ranks `0.0.6-nightly.12` below
 * `0.0.6`, which is exactly what a nightly needs to be told when the release it previews ships, and
 * below `0.0.6-nightly.13`, which is how the nightly channel finds the next nightly.
 */
fun isNewerThanRunningBuild(versionName: String): Boolean =
    SemVer.isNewer(versionName, BuildConfig.VERSION_NAME)

fun AppRelease.isRunningBuild(): Boolean = isRunningBuild(commitSha, versionName)
