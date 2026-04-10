package it.attendance100.mybicocca.data.sync

data class SyncPolicy(
    val staleAfterMs: Long,
    val timeoutMs: Long = 15_000L,
)

object SyncPolicies {
    val Default = SyncPolicy(staleAfterMs = 5 * 60_000L)
    val ShortLived = SyncPolicy(staleAfterMs = 2 * 60_000L)
    val Static = SyncPolicy(staleAfterMs = 30 * 60_000L)
}
