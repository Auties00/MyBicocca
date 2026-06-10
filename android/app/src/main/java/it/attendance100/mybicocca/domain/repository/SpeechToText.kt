package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.search.DictationEvent
import kotlinx.coroutines.flow.Flow

/**
 * Speech-to-text port behind the search dictation flow. [dictate] is cold: collection starts a
 * dictation session and cancellation stops it. The flow emits partial transcripts (and, when the
 * engine can measure it, mic sound levels) as they stream, and completes after the final
 * transcript. RECORD_AUDIO must already be granted; implementations pick the best available
 * engine.
 */
interface SpeechToText {
    fun dictate(): Flow<DictationEvent>
}
