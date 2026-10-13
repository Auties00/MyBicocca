package it.attendance100.mybicocca.core.time

import java.time.Clock

/**
 * The clock every "now" the user can see is read from, so a build can pin the app to one moment.
 * Set once at process start from the injected [Clock]. A plain object because composables and
 * top-level helpers read it too; session expiry and update bookkeeping stay on the system clock.
 */
object AppClock {
    @Volatile
    var clock: Clock = Clock.systemDefaultZone()
}
