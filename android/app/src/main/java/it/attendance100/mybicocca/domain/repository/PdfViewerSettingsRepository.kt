package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.settings.PdfPagerOrientation
import it.attendance100.mybicocca.domain.model.settings.PdfThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Persisted preferences of the in-app PDF viewer (rendering theme and scroll axis).
 *
 * The observe methods are hot flows over the settings DataStore, so an open viewer restyles
 * live when the user toggles either preference from the toolbar. The setters suspend until
 * the value is persisted.
 */
interface PdfViewerSettingsRepository {
    fun observeThemeMode(): Flow<PdfThemeMode>
    fun observeOrientation(): Flow<PdfPagerOrientation>
    suspend fun setThemeMode(mode: PdfThemeMode)
    suspend fun setOrientation(orientation: PdfPagerOrientation)
}
