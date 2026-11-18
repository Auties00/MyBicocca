package it.attendance100.mybicocca.codegen.resources

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BackendsTest {
    private fun backends() = Backends("Affinity", maxPerBackend = 2, slowSeconds = 3.0, restSeconds = 60.0, growAfter = 3)

    /** The server [backends] gives the next request, or "none" when it would wait. */
    private suspend fun next(backends: Backends): String? = withTimeoutOrNull(200) { backends.acquire() ?: "balancer" }

    @Test
    fun `lets the balancer choose until it knows a server`() = runBlocking {
        val backends = backends()
        assertEquals("balancer", next(backends))
        // One request at a time to the balancer's choice.
        assertNull(next(backends))
        backends.release(null, "a", 0.2, ok = true)
        assertEquals("a", next(backends))
    }

    @Test
    fun `sends a server more requests once it keeps answering fast`() = runBlocking {
        val backends = backends()
        next(backends)
        backends.release(null, "a", 0.2, ok = true)
        repeat(2) {
            assertEquals("a", next(backends))
            backends.release("a", null, 0.2, ok = true)
        }
        assertEquals("a", next(backends))
        assertEquals("a", next(backends))
    }

    @Test
    fun `rests a server that answers slowly`() = runBlocking {
        val backends = backends()
        next(backends)
        backends.release(null, "a", 0.2, ok = true)
        repeat(2) {
            next(backends)
            backends.release("a", null, 0.2, ok = true)
        }
        next(backends)
        backends.release("a", null, 5.0, ok = true)
        // The balancer's choice, as the only server known rests.
        assertEquals("balancer", next(backends))
        assertNull(next(backends))
    }

    @Test
    fun `rests a server that stopped answering, and leaves the others be`() = runBlocking {
        val backends = backends()
        next(backends)
        backends.release(null, "a", 0.2, ok = true)
        // An answer from another server, as one to the balancer's choice brings.
        backends.release(null, "b", 0.2, ok = true)
        val first = next(backends)!!
        backends.release(first, null, 120.0, ok = false)
        assertEquals(setOf("a", "b").minus(first).single(), next(backends))
    }

    @Test
    fun `sends a page to a server that hasn't failed it, and to one that has when none is left`() = runBlocking {
        val backends = backends()
        next(backends)
        backends.release(null, "a", 0.2, ok = true)
        backends.release(null, "b", 0.2, ok = true)
        assertEquals("b", withTimeoutOrNull(200) { backends.acquire(avoid = setOf("a")) })
        backends.release("b", null, 0.2, ok = true)
        assertEquals("a", withTimeoutOrNull(200) { backends.acquire(avoid = setOf("a", "b")) })
    }
}
