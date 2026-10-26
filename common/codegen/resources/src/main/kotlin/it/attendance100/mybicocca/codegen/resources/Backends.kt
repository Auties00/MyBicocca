package it.attendance100.mybicocca.codegen.resources

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    }

    private val lock = Mutex()
    private val backends = linkedMapOf<String, Backend>()
    private var discovering = false
    private var unpinned = 0

    /** A server with room for one more request, or null to let the balancer choose one; waits for room. */
    suspend fun acquire(): String? {
        while (true) {
            lock.withLock {
                val now = System.nanoTime()
                val free = backends.values
                    .filter { it.restingUntil <= now && it.inFlight < it.limit }
                    .minByOrNull { it.inFlight.toDouble() / it.limit }
                if (free != null) {
                    free.inFlight++
                    return free.id
                }
                if (!discovering && (unpinned < DISCOVERY_REQUESTS || backends.values.all { it.restingUntil > now })) {
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

    /** Each server's requests at a time, and whether it rests. */
    suspend fun describe(): String = lock.withLock {
        val now = System.nanoTime()
        backends.values.joinToString { "${it.id.take(8)}: ${if (it.restingUntil > now) "resting" else "${it.limit} at a time"}" }
    }

    private companion object {
        const val WAIT_MILLIS = 25L

        /** Enough requests to the balancer's choice to meet each of a few servers. */
        const val DISCOVERY_REQUESTS = 12
    }
}
