package it.attendance100.mybicocca.core.version

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

private const val META_DATA_KEY = "it.attendance100.mybicocca.BUILD_NUMBER"

/**
 * Internal build counter from `version.properties`, injected by Gradle as a manifest
 * placeholder (see the BUILD_NUMBER meta-data entry in AndroidManifest.xml). Read from
 * manifest meta-data rather than BuildConfig because AGP 9 only supports custom
 * BuildConfig fields through the variant API, whose build-time-generated constants the
 * IDE analyzer cannot resolve. aapt stores the substituted value as an int when it
 * parses as one, hence the typed read with a string fallback. Returns 0 when the entry
 * is missing or unreadable.
 */
fun buildNumber(context: Context): Int = runCatching {
    val packageManager = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    }
    val metaData = info.metaData ?: return@runCatching 0
    metaData.getInt(META_DATA_KEY).takeIf { it != 0 }
        ?: metaData.getString(META_DATA_KEY)?.toIntOrNull()
        ?: 0
}.getOrNull() ?: 0
