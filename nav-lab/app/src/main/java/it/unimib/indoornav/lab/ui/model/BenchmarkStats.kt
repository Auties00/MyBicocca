package it.unimib.indoornav.lab.ui.model

import kotlin.math.ceil

data class RendererBenchStats(
    val medianMs: Double? = null,
    val p95Ms: Double? = null,
    val maxMs: Double? = null,
    val firstCardMs: Double? = null,
    val sampleCount: Int = 0,
)

object BenchmarkStatsCalculator {

    fun summarizeNs(
        samplesNs: List<Long>,
        firstCardMs: Double? = null,
    ): RendererBenchStats {
        if (samplesNs.isEmpty()) {
            return RendererBenchStats(firstCardMs = firstCardMs, sampleCount = 0)
        }
        val sortedMs = samplesNs.map { it / 1_000_000.0 }.sorted()
        val n = sortedMs.size
        val median = if (n % 2 == 1) {
            sortedMs[n / 2]
        } else {
            (sortedMs[n / 2 - 1] + sortedMs[n / 2]) * 0.5
        }
        val p95Index = (ceil(0.95 * n).toInt() - 1).coerceIn(0, n - 1)
        return RendererBenchStats(
            medianMs = median,
            p95Ms = sortedMs[p95Index],
            maxMs = sortedMs.last(),
            firstCardMs = firstCardMs,
            sampleCount = n,
        )
    }
}
