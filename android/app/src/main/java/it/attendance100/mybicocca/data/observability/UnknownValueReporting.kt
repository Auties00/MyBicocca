package it.attendance100.mybicocca.data.observability

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.core.observability.UnknownValueSink
import it.attendance100.mybicocca.core.observability.UnknownValues
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UnknownValues"

/**
 * Sends unknown-value sightings to Crashlytics as non-fatal reports, once per field/value pair
 * per install, and only while crash reporting is enabled. A pair seen with reporting off is not
 * marked as sent, so it goes out on a later launch if the user opts in. The "already sent" record
 * holds a hash of the pair, not the pair. Debug builds also write every sighting to logcat.
 */
@Singleton
class UnknownValueReporting @Inject constructor(
    @ApplicationContext private val context: Context,
) : UnknownValueSink {

    private val sent by lazy {
        context.getSharedPreferences("unknown_values_sent", Context.MODE_PRIVATE)
    }

    fun start() {
        UnknownValues.sink = this
    }

    override fun report(field: String, value: String) {
        if (BuildConfig.DEBUG) Log.w(TAG, "$field=$value")
        runCatching {
            val crashlytics = FirebaseCrashlytics.getInstance()
            if (!crashlytics.isCrashlyticsCollectionEnabled) return
            val key = sentKey(field, value)
            if (sent.contains(key)) return
            crashlytics.recordException(UnknownValueException(field, value))
            sent.edit().putBoolean(key, true).apply()
        }
    }

    private fun sentKey(field: String, value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest("$field=$value".toByteArray())
            .joinToString("") { "%02x".format(it) }
}

/**
 * The non-fatal carrying one sighting. Its stack trace is a single synthetic frame named after
 * the field, so Crashlytics groups reports per field rather than all under the reporter itself.
 */
class UnknownValueException(field: String, value: String) : RuntimeException("$field=$value") {
    init {
        stackTrace = arrayOf(StackTraceElement("UnknownValue", field, null, -1))
    }
}
