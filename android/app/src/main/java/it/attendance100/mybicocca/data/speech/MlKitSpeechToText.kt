package it.attendance100.mybicocca.data.speech

import android.os.Build
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.audio.AudioSource
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.speechRecognizerRequest
import it.attendance100.mybicocca.domain.model.search.DictationEvent
import it.attendance100.mybicocca.domain.model.search.SpeechTranscript
import it.attendance100.mybicocca.domain.repository.SpeechToText
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Search dictation backed by the on-device ML Kit GenAI recognizer (Gemini-powered ASR),
 * configured for Italian. Only used when the model reports AVAILABLE — availability is probed
 * per session by the selector, never assumed.
 */
@Singleton
class MlKitSpeechToText @Inject constructor() : SpeechToText {

    suspend fun isAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < MIN_SDK) return false
        return runCatching {
            val recognizer = SpeechRecognition.getClient(options())
            try {
                recognizer.checkStatus() == FeatureStatus.AVAILABLE
            } finally {
                recognizer.close()
            }
        }.getOrDefault(false)
    }

    override fun dictate(): Flow<DictationEvent> = flow {
        val recognizer = SpeechRecognition.getClient(options())
        try {
            val request = speechRecognizerRequest { audioSource = AudioSource.fromMic() }
            recognizer.startRecognition(request).collect { response ->
                when (response) {
                    is SpeechRecognizerResponse.PartialTextResponse ->
                        emit(DictationEvent.Transcript(SpeechTranscript(response.text, isFinal = false)))

                    is SpeechRecognizerResponse.FinalTextResponse ->
                        emit(DictationEvent.Transcript(SpeechTranscript(response.text, isFinal = true)))

                    else -> Unit
                }
            }
        } finally {
            withContext(NonCancellable) {
                runCatching { recognizer.stopRecognition() }
                recognizer.close()
            }
        }
    }

    private fun options(): SpeechRecognizerOptions = speechRecognizerOptions {
        locale = java.util.Locale.getDefault()
        preferredMode = SpeechRecognizerOptions.Mode.MODE_ADVANCED
    }

    private companion object {
        /** ML Kit GenAI requires API 26, one above the app's minSdk of 25. */
        const val MIN_SDK = 26
    }
}
