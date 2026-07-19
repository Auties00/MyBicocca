package it.attendance100.mybicocca.core.os

import android.app.Application
import android.os.Build
import java.io.File

/**
 * Name of the current process: `it.attendance100.mybicocca` for the main one, or suffixed like
 * `it.attendance100.mybicocca:crash` for the crash-screen process. [Application.getProcessName]
 * exists only from API 28; older devices fall back to `/proc/self/cmdline`, whose arguments are
 * NUL-separated (Char.MIN_VALUE) and NUL-padded by the kernel.
 */
fun currentProcessName(): String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Application.getProcessName()
    } else {
        runCatching {
            File("/proc/self/cmdline").readText().substringBefore(Char.MIN_VALUE).trim()
        }.getOrNull()
    }
