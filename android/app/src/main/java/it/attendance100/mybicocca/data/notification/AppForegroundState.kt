package it.attendance100.mybicocca.data.notification

import androidx.annotation.MainThread
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether any of this app's UI is on screen, so a notification can decide how loudly to announce
 * itself: the user who is looking at the app has already been told by a snackbar.
 *
 * Registered from `MyBicoccaApplication.onCreate` rather than an activity, because the run where
 * the answer matters most is the one with no activity at all — a periodic check in a process the
 * user never opened. `ProcessLifecycleOwner` never leaves `CREATED` there, so this reads false,
 * which is the right answer rather than a gap.
 */
@Singleton
class AppForegroundState @Inject constructor() : DefaultLifecycleObserver {

    /** Written on the main thread, read from whichever thread a notification is posted on. */
    @Volatile
    var isForegrounded: Boolean = false
        private set

    private var started = false

    /** Idempotent: `onCreate` can run more than once per process. */
    @MainThread
    fun start() {
        if (started) return
        started = true
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        isForegrounded = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isForegrounded = false
    }
}
