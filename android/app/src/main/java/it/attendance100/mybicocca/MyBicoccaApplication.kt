package it.attendance100.mybicocca

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import it.attendance100.mybicocca.core.os.applyAppLanguage
import it.attendance100.mybicocca.core.os.systemAppLanguage

@HiltAndroidApp
class MyBicoccaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("language_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("is_system", true)) {
            applyAppLanguage(this, systemAppLanguage(this))
        }
    }
}
