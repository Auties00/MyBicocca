package it.attendance100.mybicocca.domain.model.search

/**
 * A chunk of recognized speech streamed by a dictation session, consumed by the search
 * overlay's voice input.
 *
 * @property text The recognized text so far.
 * @property isFinal Whether the engine considers the text stable; non-final transcripts
 *   may be revised by later emissions.
 */
data class SpeechTranscript(
    val text: String,
    val isFinal: Boolean,
)
