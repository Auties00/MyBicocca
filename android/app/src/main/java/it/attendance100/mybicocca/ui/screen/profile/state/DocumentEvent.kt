package it.attendance100.mybicocca.ui.screen.profile.state

// One-shot effects from ProfileViewModel (certificate download). Channel-backed, consumed
// once, never replayed across rotation.
sealed interface DocumentEvent {
    data class ShowMessage(val message: String) : DocumentEvent

    // Carries a freshly downloaded certificate PDF to the UI, which opens it in an
    // external viewer. Fired once; never replayed across rotation.
    data class OpenPdf(val bytes: ByteArray, val fileName: String) : DocumentEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is OpenPdf) return false
            return fileName == other.fileName && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = 31 * fileName.hashCode() + bytes.contentHashCode()
    }
}
