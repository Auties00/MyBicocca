package it.attendance100.mybicocca.core.crash

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Uncaught-exception handler that launches the crash screen before letting the process die.
 *
 * Registered from Application.onCreate after Firebase's Crashlytics handler, so [defaultHandler] is the Crashlytics chain:
 * delegating to it after launching the activity means the crash is still reported (when collection is enabled) and the process still terminates normally.
 *
 * The crash activity lives in its own `:crash` process, so it survives this one's death.
 * The handler is never registered in the `:crash` process itself — a crash inside the crash screen just dies to prevent a crash-loop.
 */
class GlobalExceptionHandler private constructor(
    private val applicationContext: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
    private val activityToBeLaunched: Class<*>,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        // Guarded so a failure to show the screen can never swallow the Crashlytics hand-off.
        runCatching {
            Log.e(TAG, "Uncaught exception, launching crash screen", exception)
            val intent = Intent(applicationContext, activityToBeLaunched).apply {
                putExtra(
                    EXTRA_STACK_TRACE,
                    exception.stackTraceToString().take(MAX_STACK_TRACE_CHARS)
                )
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK,
                )
            }
            applicationContext.startActivity(intent)
        }
        defaultHandler?.uncaughtException(thread, exception)
    }

    companion object {
        private const val TAG = "GlobalExceptionHandler"
        private const val EXTRA_STACK_TRACE = "stack_trace"

        /** Binder transactions cap out around 1 MB; a truncated trace still shows the failure point. */
        private const val MAX_STACK_TRACE_CHARS = 100_000

        fun initialize(applicationContext: Context, activityToBeLaunched: Class<*>) {
            Thread.setDefaultUncaughtExceptionHandler(
                GlobalExceptionHandler(
                    applicationContext,
                    Thread.getDefaultUncaughtExceptionHandler(),
                    activityToBeLaunched,
                ),
            )
        }

        fun stackTraceFromIntent(intent: Intent): String? = intent.getStringExtra(EXTRA_STACK_TRACE)
    }
}
