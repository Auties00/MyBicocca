package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.settings.FileOpenChoice
import kotlinx.coroutines.flow.Flow

/**
 * Remembered per-file-kind open choices for e-learning files.
 *
 * Keys are file kinds as classified by the viewer (image, video, audio, html, text, zip…).
 * A kind missing from the map means "ask every time": the shell shows the chooser sheet before
 * opening that file. [clearChoice] returns a kind to that state (the "Chiedi ogni volta" pick
 * in the Associazioni file settings page).
 */
interface FileOpenPreferenceRepository {
    fun observeChoices(): Flow<Map<String, FileOpenChoice>>
    suspend fun setChoice(kind: String, choice: FileOpenChoice)
    suspend fun clearChoice(kind: String)
}
