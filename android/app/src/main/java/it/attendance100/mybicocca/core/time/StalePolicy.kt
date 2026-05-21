package it.attendance100.mybicocca.core.time

data class StalePolicy(
    val defaultTtlMs: Long,
    val perSourceTtlMs: Map<String, Long> = emptyMap(),
) {
    fun ttlFor(sourceCode: String): Long = perSourceTtlMs[sourceCode] ?: defaultTtlMs
}
