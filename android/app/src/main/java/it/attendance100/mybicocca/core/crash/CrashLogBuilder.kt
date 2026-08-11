package it.attendance100.mybicocca.core.crash

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.version.buildNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Assembles the shareable crash log.
 * The file lands in `cacheDir/crash_logs/` and goes out through the system share sheet.
 */
class CrashLogBuilder(private val context: Context) {

    suspend fun dumpAndShare(stackTrace: String?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.cacheDir, "crash_logs").apply { mkdirs() }
            val file = File(dir, "mybicocca_crash_log.txt")

            file.writeText(
                debugHeader() + "\n\n" + (stackTrace
                    ?: context.getString(R.string.crash_log_no_stack_trace)) + "\n\n"
            )
            // `logcat -f` appends to the existing file, preserving the header written above.
            Runtime.getRuntime()
                .exec("logcat *:E -d -v year -v zone -f ${file.absolutePath}")
                .waitFor()

            val uri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file,
            )
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(null, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, null))
        }
    }

    private fun debugHeader(): String = """
        App ID:                 ${BuildConfig.APPLICATION_ID}
        App version:            ${BuildConfig.VERSION_NAME} (build ${buildNumber(context)}, code ${BuildConfig.VERSION_CODE})
        Android version:        ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}; build ${Build.DISPLAY})
        Device brand:           ${Build.BRAND}
        Device manufacturer:    ${Build.MANUFACTURER}
        Device name:            ${Build.DEVICE} (${Build.PRODUCT})
        Device model:           ${Build.MODEL}
        Current time:           ${OffsetDateTime.now(ZoneId.systemDefault())}
    """.trimIndent()
}
