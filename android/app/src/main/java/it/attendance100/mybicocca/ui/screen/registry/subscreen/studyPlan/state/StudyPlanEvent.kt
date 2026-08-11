package it.attendance100.mybicocca.ui.screen.registry.subscreen.studyPlan.state

import it.attendance100.mybicocca.core.text.UiText


/** One-shot effects of the percorso sheet, consumed once and never replayed. */
sealed interface StudyPlanEvent {
    /** A fetched plan-print PDF, handed off to an external viewer under [fileName]. */
    data class OpenPdf(val bytes: ByteArray, val fileName: String) : StudyPlanEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OpenPdf

            if (!bytes.contentEquals(other.bytes)) return false
            if (fileName != other.fileName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + fileName.hashCode()
            return result
        }
    }

    data class ShowMessage(val message: UiText) : StudyPlanEvent
}
