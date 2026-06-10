package it.attendance100.mybicocca.domain.model.search

/**
 * Event streamed by a dictation session alongside transcripts. Engines that can't measure
 * the input level (ML Kit GenAI) simply never emit [SoundLevel] — consumers treat it as
 * optional.
 */
sealed interface DictationEvent {
    /** A partial or final transcript chunk. */
    data class Transcript(val transcript: SpeechTranscript) : DictationEvent

    /** Microphone input level normalized to 0..1, for audio-reactive UI. */
    data class SoundLevel(val level: Float) : DictationEvent
}
