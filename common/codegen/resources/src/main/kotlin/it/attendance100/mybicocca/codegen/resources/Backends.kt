package it.attendance100.mybicocca.codegen.resources

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

/**
 * The servers behind a load balancer that keeps a client on one of them by the [cookie] it
 * sets, and that slow down each on its own once asked too much. Each request goes to a server
 * with room: at first one request at a time each, then up to [maxPerBackend] for a server that
 * keeps answering within [slowSeconds]. A server that answers slower, or not at all, or fails,
 * goes back to one at a time and rests for [restSeconds], while the others go on: a busy one
 * gets over it in a few minutes once left alone.
 *
 * The servers are learnt from the cookie of the answers to the first requests, sent to none in
 * particular one at a time, and to any sent while every known server rests.
 */
internal class Backends(
    val cookie: String,
    private val maxPerBackend: Int,
    private val slowSeconds: Double,
    private val restSeconds: Double,
    private val growAfter: Int = 20,
) {
    private class Backend(val id: String) {
        var limit = 1
        var inFlight = 0
        var fastStreak = 0
        var restingUntil = 0L

        /** How long its answers took since the last [describe]. */
        val seconds = mutableListOf<Double>()
    }

    private val lock = Mutex()
    private val backends = linkedMapOf<String, Backend>()
    private var discovering = false
    private var unpinned = 0

    /**
     * A server with room for one more request, or null to let the balancer choose one; waits
     * for room. A server in [avoid] gets it only when every other is resting: a server can keep
     * serving a page with parts missing while the others serve it whole.
     */
    suspend fun acquire(avoid: Set<String> = emptySet()): String? {
        while (true) {
            lock.withLock {
                val now = System.nanoTime()
                val awake = backends.values.filter { it.restingUntil <= now }
                val free = awake.filter { it.id !in avoid }.ifEmpty { awake }
                    .filter { it.inFlight < it.limit }
                    .minByOrNull { it.inFlight.toDouble() / it.limit }
                if (free != null) {
                    free.inFlight++
                    return free.id
                }
                if (!discovering && (unpinned < DISCOVERY_REQUESTS || awake.isEmpty())) {
                    discovering = true
                    unpinned++
                    return null
                }
            }
            delay(WAIT_MILLIS)
        }
    }

    /**
     * Ends a request sent to [sent], which [answeredBy] answered (as the balancer's cookie says,
     * when it set one) after [seconds], or not at all when [ok] is false.
     */
    suspend fun release(sent: String?, answeredBy: String?, seconds: Double, ok: Boolean) {
        lock.withLock {
            if (sent == null) discovering = false else backends[sent]?.let { it.inFlight-- }
            val backend = (answeredBy ?: sent)?.let { id -> backends.getOrPut(id) { Backend(id) } } ?: return
            if (ok) backend.seconds += seconds
            when {
                !ok || seconds > slowSeconds -> {
                    backend.limit = 1
                    backend.fastStreak = 0
                    backend.restingUntil = System.nanoTime() + (restSeconds * 1e9).toLong()
                }
                ++backend.fastStreak >= growAfter && backend.limit < maxPerBackend -> {
                    backend.limit++
                    backend.fastStreak = 0
                }
            }
        }
    }

    /** Each server's requests at a time, or whether it rests, and how fast it answered since the last time. */
    suspend fun describe(): String = lock.withLock {
        val now = System.nanoTime()
        backends.values.joinToString { backend ->
            val state = if (backend.restingUntil > now) "resting" else "${backend.limit} at a time"
            val times = backend.seconds.sorted().also { backend.seconds.clear() }
            val speed = if (times.isEmpty()) "no answers" else "${times.size} answers, median %.1f s, slowest %.1f s".format(Locale.ROOT, times[times.size / 2], times.last())
            "${backend.id.take(8)}: $state, $speed"
        }
    }

    private companion object {
        const val WAIT_MILLIS = 25L

        /** Enough requests to the balancer's choice to meet each of a few servers. */
        const val DISCOVERY_REQUESTS = 12
    }
}
