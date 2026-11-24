package it.unimib.indoornav.lab.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class VoicePrompter(context: Context) {
    private var tts: TextToSpeech? = null
    @Volatile
    private var ttsReady = false
    @Volatile
    private var pendingText: String? = null
    private val readyDeferred = CompletableDeferred<Boolean>()
    private val utteranceCounter = AtomicLong(0L)
    private val activeUtterances = ConcurrentHashMap<String, CompletableDeferred<Unit>>()
    @Volatile
    private var lastUtteranceId: String? = null

    private val toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_MUSIC, 90)
    }.getOrNull()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            var ok = false
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale.US)
                ok = res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
                if (!ok) {
                    tts?.setLanguage(Locale.getDefault())
                    ok = true
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId != null) activeUtterances.remove(utteranceId)?.complete(Unit)
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (utteranceId != null) activeUtterances.remove(utteranceId)?.complete(Unit)
                    }
                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (utteranceId != null) activeUtterances.remove(utteranceId)?.complete(Unit)
                    }
                })
                ttsReady = true
                pendingText?.let { text ->
                    pendingText = null
                    speak(text)
                }
            }
            readyDeferred.complete(ok)
        }
    }

    suspend fun awaitReady(timeoutMs: Long = 1_500L) {
        withTimeoutOrNull(timeoutMs) { readyDeferred.await() }
    }

    fun beep() {
        runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150) }
    }

    fun doubleBeep() {
        runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 250) }
    }

    fun tickBeep() {
        runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 90) }
    }

    fun speak(text: String) {
        val utteranceId = "lab_prompt_${utteranceCounter.incrementAndGet()}"
        if (!ttsReady) {
            pendingText = text
            lastUtteranceId = null
            return
        }
        val done = CompletableDeferred<Unit>()
        activeUtterances[utteranceId] = done
        lastUtteranceId = utteranceId
        val queued = runCatching {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId) == TextToSpeech.SUCCESS
        }.getOrDefault(false)
        if (!queued) {
            activeUtterances.remove(utteranceId)?.complete(Unit)
        }
    }

    suspend fun awaitLastUtterance(maxWaitMs: Long = 4_000L) {
        val id = lastUtteranceId ?: return
        val deferred = activeUtterances[id] ?: return
        withTimeoutOrNull(maxWaitMs) { deferred.await() }
    }

    fun stopSpeaking() {
        pendingText = null
        lastUtteranceId = null
        activeUtterances.values.forEach { it.complete(Unit) }
        activeUtterances.clear()
        runCatching { tts?.stop() }
    }

    fun release() {
        stopSpeaking()
        runCatching {
            tts?.shutdown()
        }
        tts = null
        runCatching { toneGenerator?.release() }
    }
}
