package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.domain.model.search.DictationEvent
import it.attendance100.mybicocca.domain.repository.SpeechToText
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams a dictation session for the search overlay's voice input: transcript chunks
 * plus, on engines that can measure it, the microphone sound level.
 */
class DictateUseCase @Inject constructor(
    private val speechToText: SpeechToText,
) {
    operator fun invoke(): Flow<DictationEvent> = speechToText.dictate()
}
