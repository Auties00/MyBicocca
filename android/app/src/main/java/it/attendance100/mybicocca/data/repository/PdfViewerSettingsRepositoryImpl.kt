package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.settings.PdfViewerSettingsStore
import it.attendance100.mybicocca.domain.model.settings.PdfPagerOrientation
import it.attendance100.mybicocca.domain.model.settings.PdfThemeMode
import it.attendance100.mybicocca.domain.repository.PdfViewerSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Delegates to [PdfViewerSettingsStore], the DataStore wrapper owning the persisted keys. */
@Singleton
class PdfViewerSettingsRepositoryImpl @Inject constructor(
    private val store: PdfViewerSettingsStore,
) : PdfViewerSettingsRepository {

    override fun observeThemeMode(): Flow<PdfThemeMode> = store.themeMode

    override fun observeOrientation(): Flow<PdfPagerOrientation> = store.orientation

    override suspend fun setThemeMode(mode: PdfThemeMode) = store.setThemeMode(mode)

    override suspend fun setOrientation(orientation: PdfPagerOrientation) = store.setOrientation(orientation)
}
