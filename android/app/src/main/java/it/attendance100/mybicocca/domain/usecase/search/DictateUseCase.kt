package it.attendance100.mybicocca.domain.usecase.search

import it.attendance100.mybicocca.domain.model.search.DictationEvent
import it.attendance100.mybicocca.domain.repository.SpeechToText
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DictateUseCase @Inject constructor(
    private val speechToText: SpeechToText,
) {
    operator fun invoke(): Flow<DictationEvent> = speechToText.dictate()
}
