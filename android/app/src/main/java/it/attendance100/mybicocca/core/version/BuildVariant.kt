package it.attendance100.mybicocca.core.version

import it.attendance100.mybicocca.BuildConfig

/** Whether this running build is itself a nightly build. */
val isNightlyBuild: Boolean = BuildConfig.VERSION_NAME.contains("nightly", ignoreCase = true)
