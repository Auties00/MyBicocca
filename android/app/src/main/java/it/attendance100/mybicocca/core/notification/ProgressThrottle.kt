package it.attendance100.mybicocca.core.notification

/**
 * Rate-limits repeated posts to the same slot.
 *
 * A download publishes progress on every 1% change, which is up to 100 posts to one slot. The
 * platform accepts only a handful of posts per second per app and silently drops the rest, so
 * without a limit the tail of a fast download is the part that gets dropped — the notification
 * appears to freeze just before finishing.
 *
 * The rule is one post per second per slot, with two exceptions that always go through: the first
 * post to a slot, and any post that isn't determinate progress. The second is what lets a terminal
 * state ("Ready to install") land immediately after a throttled 99% instead of waiting out the
 * window.
 *
 * The plan called for "one per second or per 5% change, whichever is coarser". The percentage half
 * is deliberately dropped: one per second already holds posts under the platform's limit, and
 * adding a 5% gate on top makes a slow download look stalled for as long as it takes to move five
 * points. Time alone is the honest bound.
 *
 * Not thread-safe on its own: every call must be made under the poster's lock. That orders the
 * decisions, not the posts that follow them — two threads can be granted a post in order and
 * then reach the tray out of order, briefly showing the older percentage. Self-correcting on the
 * next tick, and not worth holding a lock across a `notify()` for.
 */
class ProgressThrottle(private val minIntervalMs: Long = DEFAULT_MIN_INTERVAL_MS) {

    private val lastPostedAtMs = mutableMapOf<Int, Long>()

    /**
     * Whether a post for [spec] should go out now, [nowMs] being a monotonic clock reading. Records
     * the post when it returns true, so calling it twice for one post skews the next decision.
     */
    fun shouldPost(spec: NotificationSpec, nowMs: Long): Boolean {
        val slot = spec.id.value

        if (spec.progressPercent == null) {
            // Not a progress tick: let it through and let the next tick measure from here, so a
            // terminal post doesn't hand the following download a stale window.
            lastPostedAtMs[slot] = nowMs
            return true
        }

        val last = lastPostedAtMs[slot]
        if (last != null && nowMs - last < minIntervalMs) return false

        lastPostedAtMs[slot] = nowMs
        return true
    }

    /** Forgets a slot, so the next post to it counts as the first. */
    fun reset(id: NotificationId) {
        lastPostedAtMs.remove(id.value)
    }

    companion object {
        const val DEFAULT_MIN_INTERVAL_MS = 1_000L
    }
}
