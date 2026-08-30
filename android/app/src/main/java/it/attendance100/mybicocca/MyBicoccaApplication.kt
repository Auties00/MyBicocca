package it.attendance100.mybicocca

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import it.attendance100.mybicocca.core.crash.GlobalExceptionHandler
import it.attendance100.mybicocca.core.os.applyAppLanguage
import it.attendance100.mybicocca.core.os.currentProcessName
import it.attendance100.mybicocca.core.os.systemAppLanguage
import it.attendance100.mybicocca.data.observability.CrashReportingController
import javax.inject.Inject

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration

@HiltAndroidApp
class MyBicoccaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var crashReportingController: CrashReportingController

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // The :crash process exists only to host CrashActivity: skip the main-process boot
        // work, and crucially never install GlobalExceptionHandler there — a crash inside the
        // crash screen must just die instead of relaunching itself.
        if (currentProcessName()?.endsWith(":crash") == true) return

        crashReportingController.start()
        GlobalExceptionHandler.initialize(this, CrashActivity::class.java)

        val prefs = getSharedPreferences("language_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("is_system", true)) {
            applyAppLanguage(this, systemAppLanguage(this))
        }
    }
}
