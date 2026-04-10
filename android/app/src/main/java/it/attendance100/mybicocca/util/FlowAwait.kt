package it.attendance100.mybicocca.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

suspend fun <T : Any> Flow<T?>.awaitFirstNonNull(timeoutMs: Long = 5_000L): T =
    withTimeout(timeoutMs) {
        filterNotNull().first()
    }
