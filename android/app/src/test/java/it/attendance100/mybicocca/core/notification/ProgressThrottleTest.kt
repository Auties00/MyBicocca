package it.attendance100.mybicocca.core.notification

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * [ProgressThrottle] decides which of a download's ~100 progress posts actually reach the tray.
 * The clock is a parameter rather than a dependency, so all of this is exact rather than timed.
 */
class ProgressThrottleTest {

    private fun progress(percent: Int, id: NotificationId = NotificationId.UpdateProgress) =
        NotificationSpec(
            channel = NotificationChannelId.UPDATE_PROGRESS,
            id = id,
            title = "Downloading",
            progress = Progress.Determinate(percent),
        )

    private fun terminal(id: NotificationId = NotificationId.UpdateProgress) =
        NotificationSpec(
            channel = NotificationChannelId.UPDATE_ACTIONABLE,
            id = id,
            title = "Ready to install",
        )

    @Test
    fun `the first post to a slot always goes through`() {
        val throttle = ProgressThrottle()
        assertThat(throttle.shouldPost(progress(0), nowMs = 0)).isTrue()
    }

    @Test
    fun `a second progress post inside the window is dropped`() {
        val throttle = ProgressThrottle()
        throttle.shouldPost(progress(1), nowMs = 0)
        assertThat(throttle.shouldPost(progress(2), nowMs = 999)).isFalse()
    }

    @Test
    fun `a progress post after the window goes through`() {
        val throttle = ProgressThrottle()
        throttle.shouldPost(progress(1), nowMs = 0)
        assertThat(throttle.shouldPost(progress(2), nowMs = 1_000)).isTrue()
    }

    @Test
    fun `a big jump is still throttled`() {
        // The percentage gate the plan originally called for would have let this through; time
        // alone is the bound, so that a fast download can't outrun the platform's own limit.
        val throttle = ProgressThrottle()
        throttle.shouldPost(progress(10), nowMs = 0)
        assertThat(throttle.shouldPost(progress(90), nowMs = 500)).isFalse()
    }

    @Test
    fun `a terminal post is never throttled`() {
        // "Ready to install" must land immediately after a throttled 99%, not a second later.
        val throttle = ProgressThrottle()
        throttle.shouldPost(progress(99), nowMs = 0)
        assertThat(throttle.shouldPost(terminal(), nowMs = 1)).isTrue()
    }

    @Test
    fun `slots are throttled independently`() {
        val throttle = ProgressThrottle()
        throttle.shouldPost(progress(1, NotificationId.UpdateProgress), nowMs = 0)

        val other = NotificationId.Entity("download", "other")
        assertThat(throttle.shouldPost(progress(1, other), nowMs = 0)).isTrue()
    }

    @Test
    fun `reset makes the next post count as the first`() {
        val throttle = ProgressThrottle()
        throttle.shouldPost(progress(1), nowMs = 0)
        throttle.reset(NotificationId.UpdateProgress)

        assertThat(throttle.shouldPost(progress(2), nowMs = 1)).isTrue()
    }

    @Test
    fun `a terminal post restarts the window for the next download`() {
        // Otherwise a new download's first tick would measure against the previous download's
        // last post and sail through, then immediately throttle.
        val throttle = ProgressThrottle()
        throttle.shouldPost(terminal(), nowMs = 5_000)
        assertThat(throttle.shouldPost(progress(1), nowMs = 5_100)).isFalse()
    }
}
