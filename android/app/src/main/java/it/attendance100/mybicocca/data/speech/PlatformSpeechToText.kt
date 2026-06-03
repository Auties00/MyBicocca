package it.attendance100.mybicocca.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import it.attendance100.mybicocca.domain.model.search.DictationEvent
import it.attendance100.mybicocca.domain.model.search.SpeechTranscript
import it.attendance100.mybicocca.domain.repository.SpeechToText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

// Fallback dictation engine: the platform SpeechRecognizer (it-IT, streaming partials).
// Used on devices without the on-device ML Kit model.
@Singleton
class PlatformSpeechToText @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechToText {

    override fun dictate(): Flow<DictationEvent> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            close(IllegalStateException("Riconoscimento vocale non disponibile"))
            return@callbackFlow
        }
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults.bestTranscript()?.let {
                    trySend(DictationEvent.Transcript(SpeechTranscript(it, isFinal = false)))
                }
            }

            override fun onResults(results: Bundle?) {
                results.bestTranscript()?.let {
                    trySend(DictationEvent.Transcript(SpeechTranscript(it, isFinal = true)))
                }
                close()
            }

            override fun onError(error: Int) {
                // No-speech / no-match end the session quietly; the field just keeps its text.
                close()
            }

            override fun onRmsChanged(rmsdB: Float) {
                // The platform reports roughly -2..10 dB; squash into 0..1 for the UI.
                trySend(DictationEvent.SoundLevel(((rmsdB + 2f) / 12f).coerceIn(0f, 1f)))
            }

            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(intent)
        awaitClose {
            recognizer.stopListening()
            recognizer.destroy()
        }
        // SpeechRecognizer must be created and driven from the main thread.
    }.flowOn(Dispatchers.Main)

    private fun Bundle?.bestTranscript(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
}
