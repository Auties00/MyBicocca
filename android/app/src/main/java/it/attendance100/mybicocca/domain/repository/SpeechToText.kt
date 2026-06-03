package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.search.DictationEvent
import kotlinx.coroutines.flow.Flow

// Cold: collection starts a dictation session, cancellation stops it. Emits partial
// transcripts (and, when the engine can measure it, mic sound levels) as they stream and
// completes after the final transcript. RECORD_AUDIO must already be granted;
// implementations pick the best available engine.
interface SpeechToText {
    fun dictate(): Flow<DictationEvent>
}
