package it.attendance100.mybicocca.data.speech

import it.attendance100.mybicocca.domain.model.search.DictationEvent
import it.attendance100.mybicocca.domain.repository.SpeechToText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-session engine pick: the on-device ML Kit recognizer when its model is installed, the
 * platform SpeechRecognizer otherwise. Probing on each dictation keeps the upgrade automatic
 * once the device downloads the model.
 */
@Singleton
class SpeechToTextSelector @Inject constructor(
    private val mlKit: MlKitSpeechToText,
    private val platform: PlatformSpeechToText,
) : SpeechToText {

    override fun dictate(): Flow<DictationEvent> = flow {
        val engine = if (mlKit.isAvailable()) mlKit else platform
        emitAll(engine.dictate())
    }
}
