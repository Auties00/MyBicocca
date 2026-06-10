package it.attendance100.mybicocca.core.time

/**
 * Time-to-live policy deciding when cached data is old enough to be refreshed.
 *
 * Repositories compare a sync scope's last successful sync timestamp against [ttlFor] before
 * hitting the network, so re-entering a screen does not re-fetch data that is still fresh. The
 * app-wide instance lives in the DI graph with per-scope TTLs tuned to each source's volatility.
 *
 * @property defaultTtlMs TTL applied to any source code without a dedicated entry.
 * @property perSourceTtlMs TTL overrides keyed by sync-scope code.
 */
data class StalePolicy(
    val defaultTtlMs: Long,
    val perSourceTtlMs: Map<String, Long> = emptyMap(),
) {
    fun ttlFor(sourceCode: String): Long = perSourceTtlMs[sourceCode] ?: defaultTtlMs
}
